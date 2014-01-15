package no.hist.tdat.database;

import no.hist.tdat.database.verktoy.BrukerKoordinerer;
import no.hist.tdat.database.verktoy.EmneKoordinerer;
import no.hist.tdat.javabeans.Bruker;
import no.hist.tdat.javabeans.Emner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseConnector kobler til databasen og gjør spørring, for deretter å stenge tilkoblingen.
 *
 * @author VimCnett
 */

@Service
public class DatabaseConnector {
    private static final String QUERY_ERROR = "FEIL I SPØRRING";
    private static final String CONNECTION_ERROR = "FEIL VED TILKOBLING TIL DATABASE";
    private static final Integer ACTIVE = 1;

    // **** Legger alle Queryes her. Ikke fordi vi må, men fordi Grethe liker det sånn...*/ //TODO remove this
    private final String brukerEmnerSQL = "SELECT emner.emnekode, emner.emnenavn FROM emner, emner_brukere WHERE emner.emnekode = emner_brukere.emnekode AND emner_brukere.mail = ?";
    private final String loggInnBrukerSQL = "SELECT * FROM brukere WHERE mail = ? AND passord = ?";
    private final String leggTilBrukerSQL = "INSERT INTO brukere (mail, rettighet_id, fornavn, etternavn, passord, aktiv) VALUES (?,?,?,?,?,?)";
    private final String oppdaterBrukerSQL = "UPDATE brukere SET mail = ?, rettighet_id = ?, fornavn = ?, etternavn = ?, passord = ?, aktiv = ? WHERE mail = ?";
    private final String finnBrukerSQL = "SELECT * FROM brukere WHERE mail LIKE ? OR fornavn LIKE ? OR etternavn LIKE ?";
    private final String slettBrukerSQL = "DELETE FROM brukere WHERE mail = ?";
    private final String leggTilIKoSQL = "INSERT INTO koe_brukere (koe_id, mail, plassering, ovingsnummer, koe_plass) VALUES (?,?,?,?,?)";
    private final String finnStudentSQL = "SELECT * FROM brukere WHERE rettighet=1 AND mail LIKE ? OR fornavn LIKE ? OR etternavn LIKE ?";
    private final String endrePassordSQL = "UPDATE PASSORD FROM brukere WHERE mail LIKE ? SET passord = ?";

    @Autowired
    private DataSource dataKilde; //Felles datakilde for alle spørringer.

    /**
     * Legger til en bruker i databasen
     * @param bruker
     * @return true om den blir lagt til, ellers false
     */
    public boolean leggTilBruker(Bruker bruker) {
        if(bruker == null){
            return false;
        }
        JdbcTemplate con = new JdbcTemplate(dataKilde);
        con.update(leggTilBrukerSQL,
                bruker.getMail(),
                bruker.getRettighet(),
                bruker.getFornavn(),
                bruker.getEtternavn(),
                bruker.getPassord(),
                bruker.getAktiv());
        return true;
    }

    /**
     * Oppdatterer en spesifikk bruker
     *
     * @param bruker Den brukeren du il endre
     * @param mail   mailen til den du skal endre, denne er i tilfelle man endrer mail
     * @return true om bruker blir oppdatert ellers false
     */
    public boolean oppdaterBruker(Bruker bruker, String mail) {
        if (bruker == null) {
            return false;
        } else {
            JdbcTemplate con = new JdbcTemplate(dataKilde);
            con.update(oppdaterBrukerSQL,
                    mail,
                    bruker.getRettighet(),
                    bruker.getFornavn(),
                    bruker.getEtternavn(),
                    bruker.genererPassord(),
                    ACTIVE);
            return true;
        }
    }

    /**
     * Tar inn en string som søkeord, søker i databasen etter mail, fornavn, etternavn som ligner på søkeordet.
     *
     * @param soeketekst Søkeord etter bruker
     * @return ArrayList med bruker objekter eller null om ingen finnes.
     */
    public ArrayList<Bruker> finnBruker(String soeketekst) {
        if (soeketekst == null) {
            return null;
        }
        String input = "%";
        input += soeketekst+"%";
        JdbcTemplate con = new JdbcTemplate(dataKilde);
        List<Bruker> brukerList = con.query(finnBrukerSQL, new BrukerKoordinerer(), input, input, input);
        ArrayList<Bruker> res = new ArrayList<>();

        for (Bruker bruker : brukerList) {
            res.add(bruker);
        }
        return res;
    }

    /**
     * Sjekker om mail og passord korresponderer
     *
     * @param bruker brukerobjekt med kun mail og passord
     * @return nytt brukerobjekt med all brukerinformasjon
     */

    public Bruker loggInn(Bruker bruker){
        if (bruker == null) {
            return null;
        }
        JdbcTemplate con = new JdbcTemplate(dataKilde);
        List<Bruker> brukerList = con.query(loggInnBrukerSQL, new BrukerKoordinerer(), bruker.getMail(), bruker.getPassord());
        ArrayList<Bruker> res = new ArrayList<>();
//        System.out.println("************************ LIST LENGTH: "+brukerList.size());
        for (Bruker brukerInfo : brukerList) {
//            System.out.println("***********************************INNE I løkka ");
            res.add(brukerInfo);
        }
//        System.out.println("***********************************ETTER løkka ");
        if(res.size() >0){
//            System.out.println("***********************************IF STATEENT");
            return res.get(0);
        }
//        System.out.println("***********************************RETURN NULLZa ");
        return null;
    }
    public ArrayList<Emner> hentMineEmner(Bruker bruker){
        if (bruker == null) {
            return null;
        }
        JdbcTemplate con = new JdbcTemplate(dataKilde);
        List<Emner> emneList = con.query(brukerEmnerSQL, new EmneKoordinerer(), bruker.getMail());
        ArrayList<Emner> res = new ArrayList<>();
        for (Emner emne : emneList) {
            res.add((Emner)emne);
        }
//        System.out.println("************************ LIST LENGTH: "+brukerList.size());

//        System.out.println("***********************************ETTER løkka ");
        if(res.size() >0){
//            System.out.println("***********************************IF STATEENT");
            return res;
        }
//        System.out.println("***********************************RETURN NULLZa ");
        return null;

    }

        /**
         * Sletter bruker med gitt epost.
         *
         * @param epost eposten til den brukeren som skal slettes fra databasen
         * @return true hvis en eller flere rader fra tabellen har blitt slettet. false hvis ingen rader blir slettet.
         */
    public boolean slettBruker(String epost) {
        if (epost == null)
            return false;
        JdbcTemplate con = new JdbcTemplate(dataKilde);
        int num = con.update(slettBrukerSQL);
        return num > 0;

    }

    /**
     * Tar inn en string som søkeord, søker i databasen etter mail, fornavn, etternavn som er lik søkeordet.
     *
     * @param soeketekst Søkeord etter studenter
     * @return objekt av Bruker, eller null om den ikke finnes
     */
    public Bruker finnStudent(String soeketekst) {

        if (soeketekst == null) {
            return null;
        }

        JdbcTemplate con = new JdbcTemplate(dataKilde);
        List<Bruker> brukerList = con.query(finnStudentSQL, new BrukerKoordinerer(),soeketekst,soeketekst,soeketekst);

        return brukerList.get(0);
    }
    /**
     * Tar inn mailen til brukeren som skal endrest samt det nye passordet.
     *
     * @param passord, det nye passordet
     * @param mail, mailen til brukeren
     * @return true dersom vellykket
     */
    public boolean endrePassord(String mail, String passord){
        if(mail == null){
            return false;
        }
        JdbcTemplate con = new JdbcTemplate(dataKilde);
        con.update(endrePassordSQL,
                    mail,
                    passord);
        return true;
    }
}
