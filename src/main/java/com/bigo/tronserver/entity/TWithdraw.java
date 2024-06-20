package com.bigo.tronserver.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "t_withdraw", schema = "coinbase", catalog = "",indexes = {@Index(name="withdraw_index",columnList = "tx_id")})
public class TWithdraw {
    private int id;
    private Integer uid;
    private String coin;
    private BigDecimal money;
    private BigDecimal fee;
    private String hash;
    private String from;
    private String toAddress;
    private String remark;
    private Byte status;
    private Byte checkStatus;
    private Byte type;
    private LocalDateTime createTime;
    private Integer operatorId;
    private LocalDateTime verifyTime;
    private Integer transactionId;
    private String photo;
    private String ipAddress;
    private String position;
    private String txId;
    private Byte realStatus;
    private LocalDateTime confirmTime;
    @Column(name = "confirm_time")
    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
    }

    @Column(name = "tx_id")
    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    @Column(name = "real_status")
    public Byte getRealStatus() {
        return realStatus;
    }

    public void setRealStatus(Byte realStatus) {
        this.realStatus = realStatus;
    }

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "uid")
    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    @Basic
    @Column(name = "coin")
    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    @Basic
    @Column(name = "money")
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    @Basic
    @Column(name = "fee")
    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    @Basic
    @Column(name = "hash")
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Basic
    @Column(name = "`from`")
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Basic
    @Column(name = "to_address")
    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Basic
    @Column(name = "status")
    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    @Basic
    @Column(name = "check_status")
    public Byte getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(Byte checkStatus) {
        this.checkStatus = checkStatus;
    }

    @Basic
    @Column(name = "type")
    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    @Basic
    @Column(name = "create_time")
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "operator_id")
    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    @Basic
    @Column(name = "verify_time")
    public LocalDateTime getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(LocalDateTime verifyTime) {
        this.verifyTime = verifyTime;
    }

    @Basic
    @Column(name = "transaction_id")
    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    @Basic
    @Column(name = "photo")
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Basic
    @Column(name = "ip_address")
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Basic
    @Column(name = "position")
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TWithdraw tWithdraw = (TWithdraw) o;
        return id == tWithdraw.id && Objects.equals(uid, tWithdraw.uid) && Objects.equals(coin, tWithdraw.coin) && Objects.equals(money, tWithdraw.money) && Objects.equals(fee, tWithdraw.fee) && Objects.equals(hash, tWithdraw.hash) && Objects.equals(from, tWithdraw.from) && Objects.equals(toAddress, tWithdraw.toAddress) && Objects.equals(remark, tWithdraw.remark) && Objects.equals(status, tWithdraw.status) && Objects.equals(checkStatus, tWithdraw.checkStatus) && Objects.equals(type, tWithdraw.type) && Objects.equals(createTime, tWithdraw.createTime) && Objects.equals(operatorId, tWithdraw.operatorId) && Objects.equals(verifyTime, tWithdraw.verifyTime) && Objects.equals(transactionId, tWithdraw.transactionId) && Objects.equals(photo, tWithdraw.photo) && Objects.equals(ipAddress, tWithdraw.ipAddress) && Objects.equals(position, tWithdraw.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uid, coin, money, fee, hash, from, toAddress, remark, status, checkStatus, type, createTime, operatorId, verifyTime, transactionId, photo, ipAddress, position);
    }
}
