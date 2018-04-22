package no.knowit.fag.callcenter.backup.components;

import com.twilio.twiml.voice.Say.Language;
import lombok.Data;
import no.knowit.fag.callcenter.backup.extras.enums.MenuType;
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

    private List<MenuOption> options;

    @NotNull
    private Language language;

    private String default_queue;

    @NotNull
    private int pause;

    @NotNull
    private String waitmusic;

    @NotNull
    private MenuType menutype;

    @Data
    @Valid
    static class MenuOption {
        private String text;
        private String recording;

        private String queue;
        private String conference;

        @NotNull
        private String value;

        private List<String> dial;
        private List<MenuOption> options;
    }


    @PostConstruct
    public void init() {
        if(getOptions() != null) {return;}
        final MenuOption dummy = new MenuOption();
        dummy.setQueue(getDefault_queue());
        dummy.setValue("-1");
        options.add(dummy);
    }
}
