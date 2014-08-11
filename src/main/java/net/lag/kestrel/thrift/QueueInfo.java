/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package net.lag.kestrel.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueInfo implements org.apache.thrift.TBase<QueueInfo, QueueInfo._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("QueueInfo");

  private static final org.apache.thrift.protocol.TField HEAD_ITEM_FIELD_DESC = new org.apache.thrift.protocol.TField("head_item", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField ITEMS_FIELD_DESC = new org.apache.thrift.protocol.TField("items", org.apache.thrift.protocol.TType.I64, (short)2);
  private static final org.apache.thrift.protocol.TField BYTES_FIELD_DESC = new org.apache.thrift.protocol.TField("bytes", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField JOURNAL_BYTES_FIELD_DESC = new org.apache.thrift.protocol.TField("journal_bytes", org.apache.thrift.protocol.TType.I64, (short)4);
  private static final org.apache.thrift.protocol.TField AGE_FIELD_DESC = new org.apache.thrift.protocol.TField("age", org.apache.thrift.protocol.TType.I64, (short)5);
  private static final org.apache.thrift.protocol.TField WAITERS_FIELD_DESC = new org.apache.thrift.protocol.TField("waiters", org.apache.thrift.protocol.TType.I32, (short)6);
  private static final org.apache.thrift.protocol.TField OPEN_TRANSACTIONS_FIELD_DESC = new org.apache.thrift.protocol.TField("open_transactions", org.apache.thrift.protocol.TType.I32, (short)7);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new QueueInfoStandardSchemeFactory());
    schemes.put(TupleScheme.class, new QueueInfoTupleSchemeFactory());
  }

  public ByteBuffer head_item; // optional
  public long items; // required
  public long bytes; // required
  public long journal_bytes; // required
  public long age; // required
  public int waiters; // required
  public int open_transactions; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    HEAD_ITEM((short)1, "head_item"),
    ITEMS((short)2, "items"),
    BYTES((short)3, "bytes"),
    JOURNAL_BYTES((short)4, "journal_bytes"),
    AGE((short)5, "age"),
    WAITERS((short)6, "waiters"),
    OPEN_TRANSACTIONS((short)7, "open_transactions");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // HEAD_ITEM
          return HEAD_ITEM;
        case 2: // ITEMS
          return ITEMS;
        case 3: // BYTES
          return BYTES;
        case 4: // JOURNAL_BYTES
          return JOURNAL_BYTES;
        case 5: // AGE
          return AGE;
        case 6: // WAITERS
          return WAITERS;
        case 7: // OPEN_TRANSACTIONS
          return OPEN_TRANSACTIONS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __ITEMS_ISSET_ID = 0;
  private static final int __BYTES_ISSET_ID = 1;
  private static final int __JOURNAL_BYTES_ISSET_ID = 2;
  private static final int __AGE_ISSET_ID = 3;
  private static final int __WAITERS_ISSET_ID = 4;
  private static final int __OPEN_TRANSACTIONS_ISSET_ID = 5;
  private byte __isset_bitfield = 0;
  private _Fields optionals[] = {_Fields.HEAD_ITEM};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.HEAD_ITEM, new org.apache.thrift.meta_data.FieldMetaData("head_item", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.ITEMS, new org.apache.thrift.meta_data.FieldMetaData("items", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.BYTES, new org.apache.thrift.meta_data.FieldMetaData("bytes", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.JOURNAL_BYTES, new org.apache.thrift.meta_data.FieldMetaData("journal_bytes", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.AGE, new org.apache.thrift.meta_data.FieldMetaData("age", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.WAITERS, new org.apache.thrift.meta_data.FieldMetaData("waiters", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.OPEN_TRANSACTIONS, new org.apache.thrift.meta_data.FieldMetaData("open_transactions", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(QueueInfo.class, metaDataMap);
  }

  public QueueInfo() {
  }

  public QueueInfo(
    long items,
    long bytes,
    long journal_bytes,
    long age,
    int waiters,
    int open_transactions)
  {
    this();
    this.items = items;
    setItemsIsSet(true);
    this.bytes = bytes;
    setBytesIsSet(true);
    this.journal_bytes = journal_bytes;
    setJournal_bytesIsSet(true);
    this.age = age;
    setAgeIsSet(true);
    this.waiters = waiters;
    setWaitersIsSet(true);
    this.open_transactions = open_transactions;
    setOpen_transactionsIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public QueueInfo(QueueInfo other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetHead_item()) {
      this.head_item = org.apache.thrift.TBaseHelper.copyBinary(other.head_item);
;
    }
    this.items = other.items;
    this.bytes = other.bytes;
    this.journal_bytes = other.journal_bytes;
    this.age = other.age;
    this.waiters = other.waiters;
    this.open_transactions = other.open_transactions;
  }

  public QueueInfo deepCopy() {
    return new QueueInfo(this);
  }

  @Override
  public void clear() {
    this.head_item = null;
    setItemsIsSet(false);
    this.items = 0;
    setBytesIsSet(false);
    this.bytes = 0;
    setJournal_bytesIsSet(false);
    this.journal_bytes = 0;
    setAgeIsSet(false);
    this.age = 0;
    setWaitersIsSet(false);
    this.waiters = 0;
    setOpen_transactionsIsSet(false);
    this.open_transactions = 0;
  }

  public byte[] getHead_item() {
    setHead_item(org.apache.thrift.TBaseHelper.rightSize(head_item));
    return head_item == null ? null : head_item.array();
  }

  public ByteBuffer bufferForHead_item() {
    return head_item;
  }

  public QueueInfo setHead_item(byte[] head_item) {
    setHead_item(head_item == null ? (ByteBuffer)null : ByteBuffer.wrap(head_item));
    return this;
  }

  public QueueInfo setHead_item(ByteBuffer head_item) {
    this.head_item = head_item;
    return this;
  }

  public void unsetHead_item() {
    this.head_item = null;
  }

  /** Returns true if field head_item is set (has been assigned a value) and false otherwise */
  public boolean isSetHead_item() {
    return this.head_item != null;
  }

  public void setHead_itemIsSet(boolean value) {
    if (!value) {
      this.head_item = null;
    }
  }

  public long getItems() {
    return this.items;
  }

  public QueueInfo setItems(long items) {
    this.items = items;
    setItemsIsSet(true);
    return this;
  }

  public void unsetItems() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __ITEMS_ISSET_ID);
  }

  /** Returns true if field items is set (has been assigned a value) and false otherwise */
  public boolean isSetItems() {
    return EncodingUtils.testBit(__isset_bitfield, __ITEMS_ISSET_ID);
  }

  public void setItemsIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __ITEMS_ISSET_ID, value);
  }

  public long getBytes() {
    return this.bytes;
  }

  public QueueInfo setBytes(long bytes) {
    this.bytes = bytes;
    setBytesIsSet(true);
    return this;
  }

  public void unsetBytes() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __BYTES_ISSET_ID);
  }

  /** Returns true if field bytes is set (has been assigned a value) and false otherwise */
  public boolean isSetBytes() {
    return EncodingUtils.testBit(__isset_bitfield, __BYTES_ISSET_ID);
  }

  public void setBytesIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __BYTES_ISSET_ID, value);
  }

  public long getJournal_bytes() {
    return this.journal_bytes;
  }

  public QueueInfo setJournal_bytes(long journal_bytes) {
    this.journal_bytes = journal_bytes;
    setJournal_bytesIsSet(true);
    return this;
  }

  public void unsetJournal_bytes() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __JOURNAL_BYTES_ISSET_ID);
  }

  /** Returns true if field journal_bytes is set (has been assigned a value) and false otherwise */
  public boolean isSetJournal_bytes() {
    return EncodingUtils.testBit(__isset_bitfield, __JOURNAL_BYTES_ISSET_ID);
  }

  public void setJournal_bytesIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __JOURNAL_BYTES_ISSET_ID, value);
  }

  public long getAge() {
    return this.age;
  }

  public QueueInfo setAge(long age) {
    this.age = age;
    setAgeIsSet(true);
    return this;
  }

  public void unsetAge() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __AGE_ISSET_ID);
  }

  /** Returns true if field age is set (has been assigned a value) and false otherwise */
  public boolean isSetAge() {
    return EncodingUtils.testBit(__isset_bitfield, __AGE_ISSET_ID);
  }

  public void setAgeIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __AGE_ISSET_ID, value);
  }

  public int getWaiters() {
    return this.waiters;
  }

  public QueueInfo setWaiters(int waiters) {
    this.waiters = waiters;
    setWaitersIsSet(true);
    return this;
  }

  public void unsetWaiters() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __WAITERS_ISSET_ID);
  }

  /** Returns true if field waiters is set (has been assigned a value) and false otherwise */
  public boolean isSetWaiters() {
    return EncodingUtils.testBit(__isset_bitfield, __WAITERS_ISSET_ID);
  }

  public void setWaitersIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __WAITERS_ISSET_ID, value);
  }

  public int getOpen_transactions() {
    return this.open_transactions;
  }

  public QueueInfo setOpen_transactions(int open_transactions) {
    this.open_transactions = open_transactions;
    setOpen_transactionsIsSet(true);
    return this;
  }

  public void unsetOpen_transactions() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __OPEN_TRANSACTIONS_ISSET_ID);
  }

  /** Returns true if field open_transactions is set (has been assigned a value) and false otherwise */
  public boolean isSetOpen_transactions() {
    return EncodingUtils.testBit(__isset_bitfield, __OPEN_TRANSACTIONS_ISSET_ID);
  }

  public void setOpen_transactionsIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __OPEN_TRANSACTIONS_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case HEAD_ITEM:
      if (value == null) {
        unsetHead_item();
      } else {
        setHead_item((ByteBuffer)value);
      }
      break;

    case ITEMS:
      if (value == null) {
        unsetItems();
      } else {
        setItems((Long)value);
      }
      break;

    case BYTES:
      if (value == null) {
        unsetBytes();
      } else {
        setBytes((Long)value);
      }
      break;

    case JOURNAL_BYTES:
      if (value == null) {
        unsetJournal_bytes();
      } else {
        setJournal_bytes((Long)value);
      }
      break;

    case AGE:
      if (value == null) {
        unsetAge();
      } else {
        setAge((Long)value);
      }
      break;

    case WAITERS:
      if (value == null) {
        unsetWaiters();
      } else {
        setWaiters((Integer)value);
      }
      break;

    case OPEN_TRANSACTIONS:
      if (value == null) {
        unsetOpen_transactions();
      } else {
        setOpen_transactions((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case HEAD_ITEM:
      return getHead_item();

    case ITEMS:
      return Long.valueOf(getItems());

    case BYTES:
      return Long.valueOf(getBytes());

    case JOURNAL_BYTES:
      return Long.valueOf(getJournal_bytes());

    case AGE:
      return Long.valueOf(getAge());

    case WAITERS:
      return Integer.valueOf(getWaiters());

    case OPEN_TRANSACTIONS:
      return Integer.valueOf(getOpen_transactions());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case HEAD_ITEM:
      return isSetHead_item();
    case ITEMS:
      return isSetItems();
    case BYTES:
      return isSetBytes();
    case JOURNAL_BYTES:
      return isSetJournal_bytes();
    case AGE:
      return isSetAge();
    case WAITERS:
      return isSetWaiters();
    case OPEN_TRANSACTIONS:
      return isSetOpen_transactions();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof QueueInfo)
      return this.equals((QueueInfo)that);
    return false;
  }

  public boolean equals(QueueInfo that) {
    if (that == null)
      return false;

    boolean this_present_head_item = true && this.isSetHead_item();
    boolean that_present_head_item = true && that.isSetHead_item();
    if (this_present_head_item || that_present_head_item) {
      if (!(this_present_head_item && that_present_head_item))
        return false;
      if (!this.head_item.equals(that.head_item))
        return false;
    }

    boolean this_present_items = true;
    boolean that_present_items = true;
    if (this_present_items || that_present_items) {
      if (!(this_present_items && that_present_items))
        return false;
      if (this.items != that.items)
        return false;
    }

    boolean this_present_bytes = true;
    boolean that_present_bytes = true;
    if (this_present_bytes || that_present_bytes) {
      if (!(this_present_bytes && that_present_bytes))
        return false;
      if (this.bytes != that.bytes)
        return false;
    }

    boolean this_present_journal_bytes = true;
    boolean that_present_journal_bytes = true;
    if (this_present_journal_bytes || that_present_journal_bytes) {
      if (!(this_present_journal_bytes && that_present_journal_bytes))
        return false;
      if (this.journal_bytes != that.journal_bytes)
        return false;
    }

    boolean this_present_age = true;
    boolean that_present_age = true;
    if (this_present_age || that_present_age) {
      if (!(this_present_age && that_present_age))
        return false;
      if (this.age != that.age)
        return false;
    }

    boolean this_present_waiters = true;
    boolean that_present_waiters = true;
    if (this_present_waiters || that_present_waiters) {
      if (!(this_present_waiters && that_present_waiters))
        return false;
      if (this.waiters != that.waiters)
        return false;
    }

    boolean this_present_open_transactions = true;
    boolean that_present_open_transactions = true;
    if (this_present_open_transactions || that_present_open_transactions) {
      if (!(this_present_open_transactions && that_present_open_transactions))
        return false;
      if (this.open_transactions != that.open_transactions)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(QueueInfo other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    QueueInfo typedOther = (QueueInfo)other;

    lastComparison = Boolean.valueOf(isSetHead_item()).compareTo(typedOther.isSetHead_item());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetHead_item()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.head_item, typedOther.head_item);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetItems()).compareTo(typedOther.isSetItems());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetItems()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.items, typedOther.items);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetBytes()).compareTo(typedOther.isSetBytes());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetBytes()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.bytes, typedOther.bytes);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetJournal_bytes()).compareTo(typedOther.isSetJournal_bytes());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetJournal_bytes()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.journal_bytes, typedOther.journal_bytes);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetAge()).compareTo(typedOther.isSetAge());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAge()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.age, typedOther.age);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetWaiters()).compareTo(typedOther.isSetWaiters());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetWaiters()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.waiters, typedOther.waiters);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetOpen_transactions()).compareTo(typedOther.isSetOpen_transactions());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOpen_transactions()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.open_transactions, typedOther.open_transactions);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("QueueInfo(");
    boolean first = true;

    if (isSetHead_item()) {
      sb.append("head_item:");
      if (this.head_item == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.head_item, sb);
      }
      first = false;
    }
    if (!first) sb.append(", ");
    sb.append("items:");
    sb.append(this.items);
    first = false;
    if (!first) sb.append(", ");
    sb.append("bytes:");
    sb.append(this.bytes);
    first = false;
    if (!first) sb.append(", ");
    sb.append("journal_bytes:");
    sb.append(this.journal_bytes);
    first = false;
    if (!first) sb.append(", ");
    sb.append("age:");
    sb.append(this.age);
    first = false;
    if (!first) sb.append(", ");
    sb.append("waiters:");
    sb.append(this.waiters);
    first = false;
    if (!first) sb.append(", ");
    sb.append("open_transactions:");
    sb.append(this.open_transactions);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class QueueInfoStandardSchemeFactory implements SchemeFactory {
    public QueueInfoStandardScheme getScheme() {
      return new QueueInfoStandardScheme();
    }
  }

  private static class QueueInfoStandardScheme extends StandardScheme<QueueInfo> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, QueueInfo struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // HEAD_ITEM
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.head_item = iprot.readBinary();
              struct.setHead_itemIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ITEMS
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.items = iprot.readI64();
              struct.setItemsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // BYTES
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.bytes = iprot.readI64();
              struct.setBytesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // JOURNAL_BYTES
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.journal_bytes = iprot.readI64();
              struct.setJournal_bytesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // AGE
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.age = iprot.readI64();
              struct.setAgeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // WAITERS
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.waiters = iprot.readI32();
              struct.setWaitersIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 7: // OPEN_TRANSACTIONS
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.open_transactions = iprot.readI32();
              struct.setOpen_transactionsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, QueueInfo struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.head_item != null) {
        if (struct.isSetHead_item()) {
          oprot.writeFieldBegin(HEAD_ITEM_FIELD_DESC);
          oprot.writeBinary(struct.head_item);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldBegin(ITEMS_FIELD_DESC);
      oprot.writeI64(struct.items);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(BYTES_FIELD_DESC);
      oprot.writeI64(struct.bytes);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(JOURNAL_BYTES_FIELD_DESC);
      oprot.writeI64(struct.journal_bytes);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(AGE_FIELD_DESC);
      oprot.writeI64(struct.age);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(WAITERS_FIELD_DESC);
      oprot.writeI32(struct.waiters);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(OPEN_TRANSACTIONS_FIELD_DESC);
      oprot.writeI32(struct.open_transactions);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class QueueInfoTupleSchemeFactory implements SchemeFactory {
    public QueueInfoTupleScheme getScheme() {
      return new QueueInfoTupleScheme();
    }
  }

  private static class QueueInfoTupleScheme extends TupleScheme<QueueInfo> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, QueueInfo struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetHead_item()) {
        optionals.set(0);
      }
      if (struct.isSetItems()) {
        optionals.set(1);
      }
      if (struct.isSetBytes()) {
        optionals.set(2);
      }
      if (struct.isSetJournal_bytes()) {
        optionals.set(3);
      }
      if (struct.isSetAge()) {
        optionals.set(4);
      }
      if (struct.isSetWaiters()) {
        optionals.set(5);
      }
      if (struct.isSetOpen_transactions()) {
        optionals.set(6);
      }
      oprot.writeBitSet(optionals, 7);
      if (struct.isSetHead_item()) {
        oprot.writeBinary(struct.head_item);
      }
      if (struct.isSetItems()) {
        oprot.writeI64(struct.items);
      }
      if (struct.isSetBytes()) {
        oprot.writeI64(struct.bytes);
      }
      if (struct.isSetJournal_bytes()) {
        oprot.writeI64(struct.journal_bytes);
      }
      if (struct.isSetAge()) {
        oprot.writeI64(struct.age);
      }
      if (struct.isSetWaiters()) {
        oprot.writeI32(struct.waiters);
      }
      if (struct.isSetOpen_transactions()) {
        oprot.writeI32(struct.open_transactions);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, QueueInfo struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(7);
      if (incoming.get(0)) {
        struct.head_item = iprot.readBinary();
        struct.setHead_itemIsSet(true);
      }
      if (incoming.get(1)) {
        struct.items = iprot.readI64();
        struct.setItemsIsSet(true);
      }
      if (incoming.get(2)) {
        struct.bytes = iprot.readI64();
        struct.setBytesIsSet(true);
      }
      if (incoming.get(3)) {
        struct.journal_bytes = iprot.readI64();
        struct.setJournal_bytesIsSet(true);
      }
      if (incoming.get(4)) {
        struct.age = iprot.readI64();
        struct.setAgeIsSet(true);
      }
      if (incoming.get(5)) {
        struct.waiters = iprot.readI32();
        struct.setWaitersIsSet(true);
      }
      if (incoming.get(6)) {
        struct.open_transactions = iprot.readI32();
        struct.setOpen_transactionsIsSet(true);
      }
    }
  }

}

