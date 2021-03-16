package org.whispersystems.textsecuregcm.storage;

public class Member {
    private String userid;
    private String roleValue;
    private String profileKey;
    private String presentation;
    private int joinedAtRevision;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getRoleValue() {
        return roleValue;
    }

    public void setRoleValue(String roleValue) {
        this.roleValue = roleValue;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public int getJoinedAtRevision() {
        return joinedAtRevision;
    }

    public void setJoinedAtRevision(int joinedAtRevision) {
        this.joinedAtRevision = joinedAtRevision;
    }
}
