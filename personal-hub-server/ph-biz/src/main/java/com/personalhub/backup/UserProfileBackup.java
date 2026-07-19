package com.personalhub.backup;

import com.personalhub.system.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 用户资料备份（不含 id / username / password）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileBackup {

    private String nickname;
    private String avatar;
    private String email;
    private Integer gender;
    private LocalDate birthday;
    private String phone;
    private String country;
    private String province;
    private String city;
    private String district;
    private String website;
    private String github;
    private String bio;

    public static UserProfileBackup from(User user) {
        if (user == null) {
            return null;
        }
        return UserProfileBackup.builder()
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .phone(user.getPhone())
                .country(user.getCountry())
                .province(user.getProvince())
                .city(user.getCity())
                .district(user.getDistrict())
                .website(user.getWebsite())
                .github(user.getGithub())
                .bio(user.getBio())
                .build();
    }

    public void applyTo(User user) {
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setEmail(email);
        user.setGender(gender);
        user.setBirthday(birthday);
        user.setPhone(phone);
        user.setCountry(country);
        user.setProvince(province);
        user.setCity(city);
        user.setDistrict(district);
        user.setWebsite(website);
        user.setGithub(github);
        user.setBio(bio);
    }
}
