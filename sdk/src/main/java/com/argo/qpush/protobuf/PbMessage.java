// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: pb_message.proto

package com.argo.qpush.protobuf;

public final class PbMessage {
  private PbMessage() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_message_PBAPNSUserInfo_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_message_PBAPNSUserInfo_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_message_PBAPNSBody_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_message_PBAPNSBody_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_message_PBAPNSMessage_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_message_PBAPNSMessage_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_message_PBAPNSEvent_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_message_PBAPNSEvent_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020pb_message.proto\022\007message\",\n\016PBAPNSUse" +
      "rInfo\022\013\n\003key\030\001 \002(\t\022\r\n\005value\030\002 \002(\t\"9\n\nPBA" +
      "PNSBody\022\r\n\005alert\030\001 \001(\t\022\r\n\005sound\030\002 \001(\t\022\r\n" +
      "\005badge\030\003 \001(\005\"\254\001\n\rPBAPNSMessage\022\023\n\013offlin" +
      "eMode\030\001 \001(\005\022 \n\003aps\030\002 \002(\0132\023.message.PBAPN" +
      "SBody\022)\n\010userInfo\030\003 \003(\0132\027.message.PBAPNS" +
      "UserInfo\"9\n\014OfflineModes\022\n\n\006Ignore\020\000\022\010\n\004" +
      "APNS\020\001\022\023\n\017SendAfterOnline\020\002\"\350\001\n\013PBAPNSEv" +
      "ent\022\n\n\002op\030\001 \002(\005\022\r\n\005token\030\002 \002(\t\022\016\n\006appKey" +
      "\030\003 \002(\t\022\016\n\006userId\030\004 \002(\t\022\016\n\006typeId\030\005 \002(\005\022\014",
      "\n\004read\030\006 \001(\005\"[\n\003Ops\022\n\n\006Online\020\001\022\r\n\tKeepA" +
      "live\020\002\022\013\n\007PushAck\020\003\022\013\n\007Offline\020\004\022\t\n\005Erro" +
      "r\020\000\022\t\n\005Sleep\020\005\022\t\n\005Awake\020\006\"#\n\013DeviceTypes" +
      "\022\007\n\003iOS\020\001\022\013\n\007Android\020\002B\033\n\027com.argo.qpush" +
      ".protobufP\001"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_message_PBAPNSUserInfo_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_message_PBAPNSUserInfo_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_message_PBAPNSUserInfo_descriptor,
              new java.lang.String[] { "Key", "Value", });
          internal_static_message_PBAPNSBody_descriptor =
            getDescriptor().getMessageTypes().get(1);
          internal_static_message_PBAPNSBody_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_message_PBAPNSBody_descriptor,
              new java.lang.String[] { "Alert", "Sound", "Badge", });
          internal_static_message_PBAPNSMessage_descriptor =
            getDescriptor().getMessageTypes().get(2);
          internal_static_message_PBAPNSMessage_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_message_PBAPNSMessage_descriptor,
              new java.lang.String[] { "OfflineMode", "Aps", "UserInfo", });
          internal_static_message_PBAPNSEvent_descriptor =
            getDescriptor().getMessageTypes().get(3);
          internal_static_message_PBAPNSEvent_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_message_PBAPNSEvent_descriptor,
              new java.lang.String[] { "Op", "Token", "AppKey", "UserId", "TypeId", "Read", });
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }

  // @@protoc_insertion_point(outer_class_scope)
}