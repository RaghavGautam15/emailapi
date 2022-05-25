package com.email.controller;

import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.integration.mail.transformer.AbstractMailMessageTransformer;
import org.springframework.integration.mail.transformer.MailToStringTransformer;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.scheduling.support.PeriodicTrigger;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;
import java.util.Properties;

@Configuration(proxyBeanMethods = true)
@EnableIntegration
public class MailListenerConfig {

    @Bean
    public IntegrationFlow mailListener() {
        return IntegrationFlows.from(Mail.imapInboundAdapter(receiver()), e -> e.poller(Pollers.fixedRate(5000).maxMessagesPerPoll(1)))
//                .transform(new MailToStringTransformer())
                .<Message>handle(message -> logMail(message)).get();
    }

    private org.springframework.messaging.Message<?> logMail(org.springframework.messaging.Message<?> message) {
        System.out.println("received a mail********** !");
        System.out.println(message.getPayload());
        // process message
        MimeMessage mm = (MimeMessage) message.getPayload();
        try {
            Address[] from = mm.getFrom();
            System.out.println("From Address: " + Arrays.toString(from));
            System.out.println("Subject: " + mm.getSubject());
//            Object content = ((MimeMultipart) ((MimeMessage) message.getPayload()).getContent()).getBodyPart(0).getContent();
//            System.out.println("Content: " + content);
            Object payload = new MailToStringTransformer().transform(message).getPayload();
            System.out.println("Payload: " + payload);

            System.out.println("Body: " + readPlainContent(mm));
        } catch (javax.mail.MessagingException e) {

            System.out.println("MessagingException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return message;
    }

    public class MyMailMessageTransformer extends AbstractMailMessageTransformer<InputData> {
        protected MessageBuilder<InputData> doTransform(Message mailMessage) throws Exception {
            InputData inputData = new InputData();
            // populate inputData from message
            return MessageBuilder.withPayload(inputData);
        }
    }

    public String getMsgContent(MimeMultipart msgContent) throws MessagingException {
//        Object msgContent = messages[i].getContent();
        String content = "";
        /* Check if content is pure text/html or in parts */
        if (msgContent instanceof Multipart) {
            Multipart multipart = (Multipart) msgContent;
//            Log.e("BodyPart", "MultiPartCount: "+multipart.getCount());
            for (int j = 0; j < multipart.getCount(); j++) {
                BodyPart bodyPart = multipart.getBodyPart(j);
                String disposition = bodyPart.getDisposition();
                if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) {
                    System.out.println("Mail have some attachment");
                    DataHandler handler = bodyPart.getDataHandler();
                    System.out.println("file name : " + handler.getName());
                } else {
//                    content = getText(bodyPart);  // the changed code
                }
            }
        } else
            content = msgContent.toString();
        return content;
    }

    private static String readHtmlContent(MimeMessage message) throws Exception {
        return new MimeMessageParser(message).parse().getHtmlContent();
    }

    private static String readPlainContent(MimeMessage message) throws Exception {
        return new MimeMessageParser(message).parse().getPlainContent();
    }

    @Bean
    public ImapMailReceiver receiver() {
        //hbsskosrwphgnayb
        ImapMailReceiver receiver = new ImapMailReceiver("imaps://raghav.carvia:hbsskosrwphgnayb@imap.gmail.com:993/INBOX");
        receiver.setShouldMarkMessagesAsRead(true);
//        receiver.setJavaMailProperties(javaMailProperties());
        return receiver;
    }

    private Properties javaMailProperties() {
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        javaMailProperties.setProperty("mail.imap.socketFactory.fallback", "false");
        javaMailProperties.setProperty("mail.store.protocol", "imaps");
        javaMailProperties.setProperty("mail.debug", "true");

        return javaMailProperties;
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata defaultPoller() {
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(new PeriodicTrigger(5000));
        return pollerMetadata;
    }

//    @Bean
//    @Transformer(inputChannel="mailListener", outputChannel="stringOut")
//    public org.springframework.integration.transformer.Transformer transformer() {
//        return new MailToStringTransformer();
//    }
}
