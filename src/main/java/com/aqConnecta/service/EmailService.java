package com.aqConnecta.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void sendEmail(String email, String subject, String text) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(text, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            // Silent like a ninja
        }
    }

    public String criarCorpoEmail(String nome, String mensagem, String link) {
        return "<html>\n" +
                "<body>\n" +
                "    <table role=\"presentation\" border=\"0\" width=\"100%\">\n" +
                "        <tr>\n" +
                "            <td style=\"background-color: #19538d; width: 100%; height: 65px; text-align: center !important;\">\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "    <div style=\"width: 650px; height: 500px; overflow: hidden; margin-left: 25%;\">\n" +
                "        <table role=\"presentation\" border=\"0\" width=\"70%\"\n" +
                "            style=\"border-collapse: collapse; border: 0; overflow: hidden;\">\n" +
                "            <tr>\n" +
                "                <td colspan=\"2\" style=\"padding: 10px; text-align: left; width: 70%; border: 0; padding-top: 15px;\">\n" +
                "                    Olá <b>" + nome + "</b>,\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td colspan=\"2\" style=\"padding: 10px; text-align: left; width: 70%; border: 0;\">\n" +
                "                    " + mensagem + "\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td colspan=\"2\" style=\"padding: 10px; text-align: left; width: 70%; border: 0;\">\n" +
                "                    <a href=\"" + link + "\" target=\"_blank\" style=\"margin-left: 25%\">\n" +
                "                        <button\n" +
                "                            style=\"padding: 12px 25px 10px 25px; border: 0px; border-radius: 50px; color: #258BE3; font-size: 20px; margin: 10px 0px; font-weight: 600; letter-spacing: 2px; text-decoration: none; display: inline-block; cursor: pointer;\">\n" +
                "                            " + "Clique Aqui" +
                "                        </button>\n" +
                "                    </a>\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td colspan=\"2\" style=\"padding: 10px; text-align: left; width: 70%; border: 0;\">\n" +
                "                    Atenciosamente,\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td colspan=\"2\" style=\"padding: 10px; text-align: left; width: 70%; border: 0; padding-bottom: 10px;\">\n" +
                "                    AQConnecta.\n" +
                "                </td>\n" +
                "            </tr>\n" +
                "            </td>\n" +
                "        </table>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
