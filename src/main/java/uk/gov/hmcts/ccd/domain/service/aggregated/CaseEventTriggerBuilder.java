package uk.gov.hmcts.ccd.domain.service.aggregated;

import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.ccd.data.definition.CachedCaseDefinitionRepository;
import uk.gov.hmcts.ccd.data.definition.CaseDefinitionRepository;
import uk.gov.hmcts.ccd.data.definition.UIDefinitionRepository;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseEventTrigger;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewField;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewFieldBuilder;
import uk.gov.hmcts.ccd.domain.model.callbacks.StartEventTrigger;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseEvent;
import uk.gov.hmcts.ccd.domain.model.definition.CaseEventField;
import uk.gov.hmcts.ccd.domain.model.definition.CaseField;
import uk.gov.hmcts.ccd.domain.model.definition.CaseType;
import uk.gov.hmcts.ccd.domain.model.definition.WizardPage;
import uk.gov.hmcts.ccd.domain.service.common.EventTriggerService;
import uk.gov.hmcts.ccd.domain.service.common.ObjectMapperService;
import uk.gov.hmcts.ccd.endpoint.exceptions.ResourceNotFoundException;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

import static uk.gov.hmcts.ccd.domain.model.definition.FieldType.CASE_PAYMENT_HISTORY_VIEWER;

@Named
@Singleton
public class CaseEventTriggerBuilder {

    private final CaseDefinitionRepository caseDefinitionRepository;
    private final UIDefinitionRepository uiDefinitionRepository;
    private final EventTriggerService eventTriggerService;
    private final CaseViewFieldBuilder caseViewFieldBuilder;
    private final ObjectMapperService objectMapperService;

    public CaseEventTriggerBuilder(@Qualifier(CachedCaseDefinitionRepository.QUALIFIER) final CaseDefinitionRepository caseDefinitionRepository,
                                   final UIDefinitionRepository uiDefinitionRepository,
                                   final EventTriggerService eventTriggerService,
                                   final CaseViewFieldBuilder caseViewFieldBuilder,
                                   ObjectMapperService objectMapperService) {
        this.caseDefinitionRepository = caseDefinitionRepository;
        this.uiDefinitionRepository = uiDefinitionRepository;
        this.eventTriggerService = eventTriggerService;
        this.caseViewFieldBuilder = caseViewFieldBuilder;
        this.objectMapperService = objectMapperService;
    }

    public CaseEventTrigger build(StartEventTrigger startEventTrigger, String caseTypeId, String eventTriggerId, String caseReference) {
        if (startEventTrigger.getCaseDetails() == null) {
            throw new ResourceNotFoundException("Case not found");
        }

        final CaseType caseType = getCaseType(caseTypeId);
        final CaseEvent eventTrigger = getEventTrigger(eventTriggerId, caseType);
        final CaseEventTrigger caseEventTrigger = buildCaseEventTrigger(eventTrigger);
        hydrateHistoryField(startEventTrigger.getCaseDetails(), caseType);
        caseEventTrigger.setCaseId(caseReference);
        caseEventTrigger.setCaseFields(mergeEventFields(startEventTrigger.getCaseDetails(), caseType, eventTrigger));
        caseEventTrigger.setEventToken(startEventTrigger.getToken());
        final List<WizardPage> wizardPageCollection = uiDefinitionRepository.getWizardPageCollection(caseTypeId,
                                                                                                     eventTriggerId);
        caseEventTrigger.setWizardPages(wizardPageCollection);
        return caseEventTrigger;
    }

    private CaseType getCaseType(String caseTypeId) {
        final CaseType caseType = caseDefinitionRepository.getCaseType(caseTypeId);

        if (null == caseType) {
            throw new ResourceNotFoundException("Case type not found");
        }
        return caseType;
    }

    private CaseEvent getEventTrigger(String eventTriggerId, CaseType caseType) {
        final CaseEvent eventTrigger = eventTriggerService.findCaseEvent(caseType, eventTriggerId);

        if (null == eventTrigger) {
            throw new ResourceNotFoundException(eventTriggerId + " is not a known event ID for the specified case type: " + caseType.getId());
        }
        return eventTrigger;
    }

    private CaseEventTrigger buildCaseEventTrigger(final CaseEvent eventTrigger) {
        final CaseEventTrigger caseTrigger = new CaseEventTrigger();

        caseTrigger.setId(eventTrigger.getId());
        caseTrigger.setName(eventTrigger.getName());
        caseTrigger.setDescription(eventTrigger.getDescription());
        caseTrigger.setShowSummary(eventTrigger.getShowSummary());
        caseTrigger.setShowEventNotes(eventTrigger.getShowEventNotes());
        caseTrigger.setEndButtonLabel(eventTrigger.getEndButtonLabel());
        caseTrigger.setCanSaveDraft(eventTrigger.getCanSaveDraft());
        return caseTrigger;
    }

    private List<CaseViewField> mergeEventFields(CaseDetails caseDetails, CaseType caseType, CaseEvent eventTrigger) {
        final List<CaseEventField> eventFields = eventTrigger.getCaseFields();
        final List<CaseField> caseFields = caseType.getCaseFields();

        return caseViewFieldBuilder.build(caseFields, eventFields, caseDetails != null ? caseDetails.getCaseDataAndMetadata() : null);
    }

    void hydrateHistoryField(CaseDetails caseDetails, CaseType caseType) {
        for (CaseField caseField : caseType.getCaseFields()) {
            if (caseField.getFieldType().getType().equals(CASE_PAYMENT_HISTORY_VIEWER)) {
                caseDetails.getData().put(caseField.getId(), objectMapperService.convertObjectToJsonNode(caseDetails.getReferenceAsString()));
                return;
            }
        }
    }


}
