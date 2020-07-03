package uk.gov.hmcts.ccd.data.user;

import javax.inject.Inject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Qualifier(IdamJurisdictionsResolver.QUALIFIER)
@RequestScope
public class IdamJurisdictionsResolver implements JurisdictionsResolver {

    public static final String QUALIFIER = "default";

    private UserRepository userRepository;

    @Inject
    public IdamJurisdictionsResolver(@Qualifier(CachedUserRepository.QUALIFIER) UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<String> getJurisdictions() {
        return this.userRepository.getUserRolesJurisdictions();
    }
}
