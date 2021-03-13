// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Groups.proto

package org.whispersystems.textsecuregcm.proto;

public interface GroupChangeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:GroupsProtos.GroupChange)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The serialized actions
   * </pre>
   *
   * <code>bytes actions = 1;</code>
   * @return The actions.
   */
  com.google.protobuf.ByteString getActions();

  /**
   * <pre>
   * Server’s signature over serialized actions
   * </pre>
   *
   * <code>bytes serverSignature = 2;</code>
   * @return The serverSignature.
   */
  com.google.protobuf.ByteString getServerSignature();

  /**
   * <code>uint32 changeEpoch = 3;</code>
   * @return The changeEpoch.
   */
  int getChangeEpoch();
}
