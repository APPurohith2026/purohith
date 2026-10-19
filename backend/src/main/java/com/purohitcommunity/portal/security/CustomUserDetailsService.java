package com.purohitcommunity.portal.security;

import com.purohitcommunity.portal.domain.Enums.ApplicationStatus;
import com.purohitcommunity.portal.domain.UserAccount;
import com.purohitcommunity.portal.repository.MemberProfileRepository;
import com.purohitcommunity.portal.repository.UserAccountRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserAccountRepository users;
  private final MemberProfileRepository members;

  private static final Pattern MEMBER_ID_PATTERN =
          Pattern.compile("^(AP|TS)-[A-Z0-9]+-\\d{6}$");

  private static final Pattern PHONE_PATTERN =
          Pattern.compile("^\\d{10,15}$");

  @Override
  public UserDetails loadUserByUsername(String identifier)
          throws UsernameNotFoundException {

    UserAccount user = resolve(identifier);

    return User
            .withUsername(canonicalLogin(user))
            .password(user.getPasswordHash())
            .roles(user.getRole().name())
            .disabled(!user.isEnabled())
            .build();
  }

  @Transactional(readOnly = true)
  public UserAccount resolve(String rawIdentifier) {

    if (rawIdentifier == null || rawIdentifier.isBlank()) {
      throw new UsernameNotFoundException(
              "Phone number, Member ID or Admin email is required"
      );
    }

    String identifier = rawIdentifier.trim();

    /*
     * 1. Generated Member ID
     *
     * Example:
     * TS-HYD-000003
     */
    String upperIdentifier =
            identifier.toUpperCase(Locale.ROOT);

    if (MEMBER_ID_PATTERN.matcher(upperIdentifier).matches()) {

      var member = members
              .findByMemberNumberIgnoreCase(upperIdentifier)
              .orElseThrow(() ->
                      new UsernameNotFoundException(
                              "Member ID not found"
                      )
              );

      if (member.getApplicationStatus()
              != ApplicationStatus.APPROVED) {

        throw new UsernameNotFoundException(
                "Member is not approved"
        );
      }

      UserAccount user = member.getUser();

      if (user == null || !user.isEnabled()) {
        throw new UsernameNotFoundException(
                "Member account is disabled"
        );
      }

      return user;
    }

    /*
     * 2. Admin email
     */
    if (identifier.contains("@")) {

      return users
              .findByEmailIgnoreCase(identifier)
              .orElseThrow(() ->
                      new UsernameNotFoundException(
                              "Email account not found"
                      )
              );
    }

    /*
     * 3. Phone number
     */
    String normalizedPhone =
            identifier.replaceAll("\\D", "");

    if (PHONE_PATTERN.matcher(normalizedPhone).matches()) {

      return users
              .findByPhoneNumber(normalizedPhone)
              .orElseThrow(() ->
                      new UsernameNotFoundException(
                              "Phone number not found"
                      )
              );
    }

    throw new UsernameNotFoundException(
            "Invalid login identifier"
    );
  }

  public String canonicalLogin(UserAccount user) {

    if (user.getPhoneNumber() != null
            && !user.getPhoneNumber().isBlank()) {

      return user.getPhoneNumber();
    }

    if (user.getEmail() != null
            && !user.getEmail().isBlank()) {

      return user.getEmail();
    }

    throw new UsernameNotFoundException(
            "Account has no valid login identifier"
    );
  }
}