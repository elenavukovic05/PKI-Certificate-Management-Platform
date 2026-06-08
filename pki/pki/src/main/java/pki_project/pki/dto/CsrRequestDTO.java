package pki_project.pki.dto;

import java.util.Date;

public class CsrRequestDTO {
    private String csrPem;
    private Long issuerId;
    private Date startDate;
    private Date endDate;

    public String getCsrPem() {
        return csrPem;
    }
    public void setCsrPem(String csrPem) {
        this.csrPem = csrPem;
    }
    public Long getIssuerId() {
        return issuerId;
    }
    public void setIssuerId(Long issuerId) {
        this.issuerId = issuerId;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}