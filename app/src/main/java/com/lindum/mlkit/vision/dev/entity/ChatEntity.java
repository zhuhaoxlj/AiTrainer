package com.lindum.mlkit.vision.dev.entity;

/**
 * @author YipJing
 * @date 2022/3/7 14:58
 */
public class ChatEntity {
    //昵称
    private final String nickName;
    //时间
    private final String time;
    //聊天内容
    private final String chatContent;
    //头像图片
    private final int imageId;

    public ChatEntity(String nickName, String time, String chatContent, int imageId) {
        this.nickName = nickName;
        this.time = time;
        this.chatContent = chatContent;
        this.imageId = imageId;
    }

    public String getNickName() {
        return nickName;
    }

    public String getTime() {
        return time;
    }

    public String getChatContent() {
        return chatContent;
    }

    public int getImageId() {
        return imageId;
    }
}
