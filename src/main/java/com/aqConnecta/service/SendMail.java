package com.aqConnecta.service;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {
    public static void main(String[] args) {
      String to = "emailpara@gmail.com";
      String from = "aqconnectaverificador@gmail.com";
      String host = "smtp.gmail.com";

      Properties properties = System.getProperties();
      properties.put("mail.smtp.host", host);
      properties.put("mail.smtp.port", "465");
      properties.put("mail.smtp.auth", "true");
      properties.put("mail.smtp.starttls.enable", "true");
      properties.put("mail.smtp.starttls.required", "true");
      properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
      properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

      Session session = Session.getInstance(properties, new javax.mail.Authenticator(){
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication("aqconnectaverificador@gmail.com", "");
        }
      });

      try {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject("This is the email subject");
        message.setText("Vai se fuder davi, pau no cu do juan.... na vdd no cu dele nao, ele gosta....");

        Transport.send(message);
      } catch (MessagingException mex) {
        mex.printStackTrace();
      }
   }
}
