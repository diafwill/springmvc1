package edu.mum.service.impl;

import edu.mum.domain.Message;
import edu.mum.repository.MessageRepository;
import edu.mum.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    MessageRepository messageRepository;

    @Override
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    @Override
    public List<Message> getMessages() {
        return (List<Message>) messageRepository.findAll();
    }

    @Override
    public Message getMessageById(Long id) {
        return messageRepository.findById(id).get();
    }

    @Override
    public void setMessageRead(Long id) {
        messageRepository.setMessageRead(id);
    }
}
