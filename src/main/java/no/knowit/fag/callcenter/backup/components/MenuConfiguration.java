package no.knowit.fag.callcenter.backup.components;

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
    static class MenuOption {
        private String text;
        private String recording;

        private String queue;

        @NotNull
        private String value;

        private List<MenuOption> options;
    }

    private MenuOption dummy = new MenuOption();

    @PostConstruct
    public void init() {
        dummy.setQueue(getDefault_queue());
        dummy.setValue("default");
        options.add(dummy);
    }
}
