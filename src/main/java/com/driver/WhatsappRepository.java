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
        userMobile.add(mobile);
        User user=new User(name,mobile);
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
        if(users.size()==2){
            Group group=new Group(users.get(1).getName(),2);
            adminMap.put(group,users.get(0));
            groupUserMap.put(group,users);
            groupMessageMap.put(group,new ArrayList<Message>());
            return group;

        }
            this.customGroupCount+=1;
        Group group=new Group(new String("Group"+this.customGroupCount),users.size());
        adminMap.put(group,users.get(0));
        groupUserMap.put(group,users);
        groupMessageMap.put(group,new ArrayList<Message>());
        return group;
    }

    public int createMessage(String content) {
        messageId+=1;
        Message message=new Message(messageId,content);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group)throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(adminMap.containsKey(group)) {
            List<User> users = groupUserMap.get(group);
            boolean userFound = false;
            for (User user : users) {
                if(user.equals(sender)){
                    userFound=true;
                    break;
            }
        }
            if (userFound){
                senderMap.put(message,sender);
                List<Message>messages=groupMessageMap.get(group);
                messages.add(message);
                groupMessageMap.put(group,messages);
                return messages.size();
            }
        throw new Exception("You are not allowed to send message");
        }
        throw new Exception("Group Does not Exist");
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS".
        // Note that at one time there is only one admin and the admin rights are transferred
        // from approver to user.
        if (adminMap.containsKey(group)) {
            if (adminMap.get(group).equals(approver)) {
                List<User> participants = groupUserMap.get(group);
                boolean userFound = false;
                for (User participant : participants) {
                    if (participant.equals(user)) {
                        userFound = true;
                        break;
                    }
                }
                if (userFound) {
                    adminMap.put(group, user);
                    return "SUCCESS";
                }
                throw new Exception("User is not a participant");
            }
            throw new Exception("Approver does not have rights");
        }
        throw new Exception("Group does not exist");
    }

    public int removeUser(User user) throws Exception {
//        Group group = findGroupByUser(user);
//
//        if (group == null) {
//            throw new Exception("User not found");
//        }
//        String admin= String.valueOf(adminMap.get(user));
//        if (admin.equals(user)) {
//            throw new Exception("Cannot remove admin");
//        }
//
//        List<User> members = groupUserMap.get(group);
//        members.remove(user);
//        groupUserMap.put(group, members);
//
//        // Remove user's messages from groupMessageMap
//        List<Message> messages = groupMessageMap.getOrDefault(group, new ArrayList<>());
//        messages.removeIf(message -> senderMap.get(message).equals(user));
//        groupMessageMap.put(group, messages);
//
//        // Update relevant attributes
//        int updatedNumUsersInGroup = members.size();
//        int updatedNumMessagesInGroup = messages.size();
//        int updatedNumOverallMessages = senderMap.size();
//        return updatedNumUsersInGroup + updatedNumMessagesInGroup + updatedNumOverallMessages;
        boolean userFound=false;
        Group userGroup=null;
        for(Group group:groupUserMap.keySet()) {
            List<User> participants = groupUserMap.get(group);
            for (User participant : participants) {
                if (participant.equals(user)) {
                    if (adminMap.get(group).equals(user)) {
                        throw new Exception("Cannot remove admin");
                    }
                    userGroup = group;
                    userFound = true;
                    break;
                }
            }
            if (userFound) {
                break;
            }
        }
        if(userFound) {
            List<User> users = groupUserMap.get(userGroup);
            List<User> updateUsers = new ArrayList<>();
            for (User participant : users) {
                if (participant.equals(user)) {
                    continue;
                }
                updateUsers.add(participant);
            }
            groupUserMap.put(userGroup, updateUsers);

            List<Message> messages = groupMessageMap.get(userGroup);
            List<Message> updateMessages = new ArrayList<>();
            for (Message message : messages) {
                if (senderMap.get(message).equals(user))
                    continue;
                updateMessages.add(message);
            }
            groupMessageMap.put(userGroup, updateMessages);
            HashMap<Message, User> updateSenderMap = new HashMap<>();
            for (Message message : senderMap.keySet()) {
                if (senderMap.get(message).equals(user))
                    continue;
                updateSenderMap.put(message, senderMap.get(message));
            }
            senderMap = updateSenderMap;
            return updateUsers.size()+ updateMessages.size()+ updateSenderMap.size();
        }
        throw new Exception("User Not Found");

    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message> messageses = new ArrayList<>();
        for(Group group:groupMessageMap.keySet()){
            messageses.addAll(groupMessageMap.get(group));
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
