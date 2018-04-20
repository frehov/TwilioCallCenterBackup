package no.knowit.fag.callcenter.backup.components;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Pause;
import com.twilio.twiml.voice.Play;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Say.Language;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.logging.Logger;

import static com.twilio.twiml.voice.Gather.Input.DTMF;
import static java.util.stream.Collectors.toList;

@Component
@Profile("menu")
@ConfigurationProperties(prefix = "ivr.menu")
@Data
@Validated
public class MenuConfiguration {

    @NotNull
    private List<MenuOption> options;

    @NotNull
    private Language language;

    @NotNull
    private String default_queue;

    @NotNull
    private int pause;

    @NotNull
    private String waitmusic;

    private String menutype;

    @Data
    @Valid
    public static class MenuOption {
        private String text;
        private String recording;

        private String queue;
        private String route;

        @NotNull
        private String value;

        private List<MenuOption> options;
    }

    private MenuOption dummy = new MenuOption();

    @PostConstruct
    public void init() {
        dummy.setQueue(getDefault_queue());
    }
}
