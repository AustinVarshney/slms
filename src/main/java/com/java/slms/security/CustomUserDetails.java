package com.java.slms.security;

import com.java.slms.model.User;
import com.java.slms.util.RoleEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
@Setter
public class CustomUserDetails implements UserDetails
{

    private final User user;
    private Long schoolId;

    public CustomUserDetails(User user, Long schoolId)
    {
        this.user = user;
        this.schoolId = schoolId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return user.getRoles().stream().map(RoleEnum::name).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public String getPassword()
    {
        return user.getPassword();
    }

    // username for Spring Security we can return email if present else pan
    @Override
    public String getUsername()
    {
        return user.getEmail() != null ? user.getEmail() : user.getPanNumber();
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return user.isEnabled();
    }

    public Long getId()
    {
        return user.getId();
    }

    public String getEmail()
    {
        return user.getEmail();
    }

    public String getPanNumber()
    {
        return user.getPanNumber();
    }

}
