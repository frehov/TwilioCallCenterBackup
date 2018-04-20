package no.knowit.fag.callcenter.backup.utils;

import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class WriteTwiml {

    private static final Logger log = Logger.getLogger(WriteTwiml.class.toGenericString());

    public static void write(VoiceResponse voiceResponse, HttpServletResponse response)  {
        try {
            response.setContentType("text/xml;charset=UTF-8");
            response.getWriter().write(voiceResponse.toXml());
        } catch (TwiMLException |IOException e){
            throw new RuntimeException(e);
        } finally {
            log.info(voiceResponse.toXml());
        }
    }
}
