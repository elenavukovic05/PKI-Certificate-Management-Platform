package pki_project.pki.dto;

public class RegisterDTO {
    String username;
    String password;
    String confirmedPassword;
    String name;
    String lastName;
    Long organizationId;
    String publicKey;

    public RegisterDTO(String username, String password, String confirmedPassword,
                       String name, String lastName, Long organizationId, String publicKey) {
        this.username = username;
        this.password = password;
        this.confirmedPassword = confirmedPassword;
        this.name = name;
        this.lastName = lastName;
        this.organizationId = organizationId;
        this.publicKey = publicKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmedPassword() {
        return confirmedPassword;
    }

    public void setConfirmedPassword(String confirmedPassword) {
        this.confirmedPassword = confirmedPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
