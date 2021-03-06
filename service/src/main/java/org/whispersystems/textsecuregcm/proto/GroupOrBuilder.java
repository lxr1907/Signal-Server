// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Groups.proto

package org.whispersystems.textsecuregcm.proto;

public interface GroupOrBuilder extends
    // @@protoc_insertion_point(interface_extends:GroupsProtos.Group)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * GroupPublicParams
   * </pre>
   *
   * <code>bytes publicKey = 1;</code>
   * @return The publicKey.
   */
  com.google.protobuf.ByteString getPublicKey();

  /**
   * <pre>
   * Encrypted title
   * </pre>
   *
   * <code>bytes title = 2;</code>
   * @return The title.
   */
  com.google.protobuf.ByteString getTitle();

  /**
   * <pre>
   * Pointer to encrypted avatar (‘key’ from AvatarUploadAttributes)
   * </pre>
   *
   * <code>string avatar = 3;</code>
   * @return The avatar.
   */
  String getAvatar();
  /**
   * <pre>
   * Pointer to encrypted avatar (‘key’ from AvatarUploadAttributes)
   * </pre>
   *
   * <code>string avatar = 3;</code>
   * @return The bytes for avatar.
   */
  com.google.protobuf.ByteString
      getAvatarBytes();

  /**
   * <pre>
   * Encrypted timer
   * </pre>
   *
   * <code>bytes disappearingMessagesTimer = 4;</code>
   * @return The disappearingMessagesTimer.
   */
  com.google.protobuf.ByteString getDisappearingMessagesTimer();

  /**
   * <code>.GroupsProtos.AccessControl accessControl = 5;</code>
   * @return Whether the accessControl field is set.
   */
  boolean hasAccessControl();
  /**
   * <code>.GroupsProtos.AccessControl accessControl = 5;</code>
   * @return The accessControl.
   */
  org.whispersystems.textsecuregcm.proto.AccessControl getAccessControl();
  /**
   * <code>.GroupsProtos.AccessControl accessControl = 5;</code>
   */
  org.whispersystems.textsecuregcm.proto.AccessControlOrBuilder getAccessControlOrBuilder();

  /**
   * <pre>
   * Current group revision number
   * </pre>
   *
   * <code>uint32 revision = 6;</code>
   * @return The revision.
   */
  int getRevision();

  /**
   * <code>repeated .GroupsProtos.Member members = 7;</code>
   */
  java.util.List<org.whispersystems.textsecuregcm.proto.Member>
      getMembersList();
  /**
   * <code>repeated .GroupsProtos.Member members = 7;</code>
   */
  org.whispersystems.textsecuregcm.proto.Member getMembers(int index);
  /**
   * <code>repeated .GroupsProtos.Member members = 7;</code>
   */
  int getMembersCount();
  /**
   * <code>repeated .GroupsProtos.Member members = 7;</code>
   */
  java.util.List<? extends org.whispersystems.textsecuregcm.proto.MemberOrBuilder>
      getMembersOrBuilderList();
  /**
   * <code>repeated .GroupsProtos.Member members = 7;</code>
   */
  org.whispersystems.textsecuregcm.proto.MemberOrBuilder getMembersOrBuilder(
      int index);

  /**
   * <code>repeated .GroupsProtos.PendingMember pendingMembers = 8;</code>
   */
  java.util.List<org.whispersystems.textsecuregcm.proto.PendingMember>
      getPendingMembersList();
  /**
   * <code>repeated .GroupsProtos.PendingMember pendingMembers = 8;</code>
   */
  org.whispersystems.textsecuregcm.proto.PendingMember getPendingMembers(int index);
  /**
   * <code>repeated .GroupsProtos.PendingMember pendingMembers = 8;</code>
   */
  int getPendingMembersCount();
  /**
   * <code>repeated .GroupsProtos.PendingMember pendingMembers = 8;</code>
   */
  java.util.List<? extends org.whispersystems.textsecuregcm.proto.PendingMemberOrBuilder>
      getPendingMembersOrBuilderList();
  /**
   * <code>repeated .GroupsProtos.PendingMember pendingMembers = 8;</code>
   */
  org.whispersystems.textsecuregcm.proto.PendingMemberOrBuilder getPendingMembersOrBuilder(
      int index);
}
