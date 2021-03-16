package org.whispersystems.textsecuregcm.storage;

import org.signal.zkgroup.auth.AuthCredentialPresentation;
import org.signal.zkgroup.auth.AuthCredentialResponse;
import org.signal.zkgroup.groups.GroupPublicParams;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

public class GroupEntity implements Principal {
    private AuthCredentialPresentation authCredentialPresentation;
    private AuthCredentialResponse authCredentialResponse;
    private UUID uuid;
    //年月日
    private int date;

    private GroupPublicParams groupPublicParams;
    private String title;
    private String avatar;
    private String disappearingMessagesTimer;
    /**
     * <code>UNKNOWN = 0;</code>
     * <code>ANY = 1;</code>
     * <code>MEMBER = 2;</code>
     * <code>ADMINISTRATOR = 3;</code>
     */
    private int accessControl;
    private int revision;
    private List<Member> memberList;
    private List<Member> pendingMembers;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDisappearingMessagesTimer() {
        return disappearingMessagesTimer;
    }

    public void setDisappearingMessagesTimer(String disappearingMessagesTimer) {
        this.disappearingMessagesTimer = disappearingMessagesTimer;
    }

    public int getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(int accessControl) {
        this.accessControl = accessControl;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public List<Member> getMemberList() {
        return memberList;
    }

    public void setMemberList(List<Member> memberList) {
        this.memberList = memberList;
    }

    public List<Member> getPendingMembers() {
        return pendingMembers;
    }

    public void setPendingMembers(List<Member> pendingMembers) {
        this.pendingMembers = pendingMembers;
    }

    public AuthCredentialResponse getAuthCredentialResponse() {
        return authCredentialResponse;
    }

    public void setAuthCredentialResponse(AuthCredentialResponse authCredentialResponse) {
        this.authCredentialResponse = authCredentialResponse;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public AuthCredentialPresentation getAuthCredentialPresentation() {
        return authCredentialPresentation;
    }

    public void setAuthCredentialPresentation(AuthCredentialPresentation authCredentialPresentation) {
        this.authCredentialPresentation = authCredentialPresentation;
    }

    public GroupPublicParams getGroupPublicParams() {
        return groupPublicParams;
    }

    public void setGroupPublicParams(GroupPublicParams groupPublicParams) {
        this.groupPublicParams = groupPublicParams;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}
