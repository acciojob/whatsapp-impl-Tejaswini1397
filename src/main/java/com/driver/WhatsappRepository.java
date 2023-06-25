package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        if(userMobile.contains(mobile) ){
             throw new Exception("User is already Exist");
        }
        User user=new User(name,mobile);
        userMobile.add(mobile);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users)  {
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.
        // If group is successfully created, return group.

        //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.
        if(users.size()<2){
            throw new IllegalArgumentException("At least 2 users required");
        }
        User admin=users.get(0);
        List<User>member=new ArrayList<>(users.subList(1,users.size()));
        String groupName="";
        if(users.size()==2){
            groupName=member.get(0).getName();
        }else{
            customGroupCount++;
            groupName="Group"+customGroupCount;
        }
        Group group=new Group(groupName,member.size());
        groupUserMap.put(group,member);
        adminMap.put(group,admin);
        return group;
    }

    public int createMessage(String content) {
        messageId++;
        Message message=new Message(messageId,content);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(!groupUserMap.containsKey(group)){
            throw new IllegalArgumentException("Group does not exist");
        }
        List<User>member=groupUserMap.get(group);
        if(!member.contains(sender)){
            throw new IllegalArgumentException("You are not allowed to send message");
        }
        senderMap.put(message,sender);
        List<Message>messages=groupMessageMap.getOrDefault(group,new ArrayList<>());
        messages.add(message);
        groupMessageMap.put(group,messages);
        return messages.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS".
        // Note that at one time there is only one admin and the admin rights are transferred
        // from approver to user.
        if(!groupUserMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        User currAdmin=adminMap.get(group);
        if(!currAdmin.equals(approver)){
            throw new Exception("Approver does not have rights");
        }
        List<User>member=groupUserMap.get(group);
        if(!member.contains(user)){
            throw new Exception("User is not a participant");
        }
        adminMap.put(group,user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        Group group = findGroupByUser(user);

        if (group == null) {
            throw new Exception("User not found");
        }
        String admin= String.valueOf(adminMap.get(user));
        if (admin.equals(user)) {
            throw new Exception("Cannot remove admin");
        }

        List<User> members = groupUserMap.get(group);
        members.remove(user);
        groupUserMap.put(group, members);

        // Remove user's messages from groupMessageMap
        List<Message> messages = groupMessageMap.getOrDefault(group, new ArrayList<>());
        messages.removeIf(message -> senderMap.get(message).equals(user));
        groupMessageMap.put(group, messages);

        // Update relevant attributes
        int updatedNumUsersInGroup = members.size();
        int updatedNumMessagesInGroup = messages.size();
        int updatedNumOverallMessages = senderMap.size();
        return updatedNumUsersInGroup + updatedNumMessagesInGroup + updatedNumOverallMessages;
    }
    private Group findGroupByUser(User user) {
        for (Map.Entry<Group, List<User>> entry : groupUserMap.entrySet()) {
            if (entry.getValue().contains(user)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message> messageses = new ArrayList<>();
        for(Group group:groupMessageMap.keySet()){
            if(messageses.addAll(groupMessageMap.get(group)));
        }
        List<Message>filterMessages=new ArrayList<>();
        for(Message message:messageses){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
                filterMessages.add(message);
            }
        }

        if (filterMessages.size() < k) {
            throw new Exception("K is greater than the number of messages");
        }
        // Sort the messages in descending order of timestamp
        Collections.sort(filterMessages, new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                    }
                });

        return filterMessages.get(k-1).getContent();
    }

}
