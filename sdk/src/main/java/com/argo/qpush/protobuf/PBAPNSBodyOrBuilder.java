// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: pb_message.proto

package com.argo.qpush.protobuf;

public interface PBAPNSBodyOrBuilder
    extends com.google.protobuf.MessageOrBuilder {

  // optional string alert = 1;
  /**
   * <code>optional string alert = 1;</code>
   */
  boolean hasAlert();
  /**
   * <code>optional string alert = 1;</code>
   */
  java.lang.String getAlert();
  /**
   * <code>optional string alert = 1;</code>
   */
  com.google.protobuf.ByteString
      getAlertBytes();

  // optional string sound = 2;
  /**
   * <code>optional string sound = 2;</code>
   */
  boolean hasSound();
  /**
   * <code>optional string sound = 2;</code>
   */
  java.lang.String getSound();
  /**
   * <code>optional string sound = 2;</code>
   */
  com.google.protobuf.ByteString
      getSoundBytes();

  // optional int32 badge = 3;
  /**
   * <code>optional int32 badge = 3;</code>
   */
  boolean hasBadge();
  /**
   * <code>optional int32 badge = 3;</code>
   */
  int getBadge();
}