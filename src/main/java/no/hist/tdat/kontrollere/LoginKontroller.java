package no.hist.tdat.kontrollere;

import no.hist.tdat.javabeans.Bruker;
import no.hist.tdat.javabeans.beanservice.BrukerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Enumeration;


@Controller
public class LoginKontroller {

    @Autowired
    BrukerService service;

    @RequestMapping(value = "loggerinn.SSL", method= RequestMethod.POST)
    private String loggerInn(@Valid @ModelAttribute("bruker") Bruker bruker, BindingResult result,HttpSession session) {
        if(result.hasErrors()){
            return "loggInn";
        }
        bruker=bruker.loggInn();
        if(bruker==null){
            return "loggInn";
        }
        Bruker brook = service.loggInn(bruker);
        if(brook==null){
            return "/login.htm";
        }
        session.setAttribute("innloggetBruker", bruker);
        return "minside";
    }

    @RequestMapping("/login.htm")
    public String loggInn(@ModelAttribute("bruker") Bruker bruker){
        return "loggInn";
    }

    @RequestMapping("/")
    public String omdirigerHjem() {
        return "index";
    }
}
