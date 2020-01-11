package com.jd.netty.mssagepack.domain;

import org.msgpack.annotation.Message;

import java.util.Date;

/**
 * Created by Administrator on 2018/11/25 0025.
 * 用户 实体
 * 需要序列化的 POJO 对象上必须加上 org.msgpack.annotation.Message 注解：@Message
 */
@Message
public class User {
    private Integer pId;
    private String pName;
    private Date birthday;
    private Boolean isMarry;

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Integer getpId() {
        return pId;
    }

    public void setpId(Integer pId) {
        this.pId = pId;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public Boolean getIsMarry() {
        return isMarry;
    }

    public void setIsMarry(Boolean isMarry) {
        this.isMarry = isMarry;
    }

    @Override
    public String toString() {
        return "User{" +
                "birthday=" + birthday +
                ", pId=" + pId +
                ", pName='" + pName + '\'' +
                ", isMarry=" + isMarry +
                '}';
    }
}