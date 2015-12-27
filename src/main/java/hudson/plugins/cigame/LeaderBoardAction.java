package hudson.plugins.cigame;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import hudson.Extension;
import hudson.model.*;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.util.VersionNumber;

import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Leader board for users participaing in the game.
 * 
 * @author Erik Ramfelt
 */
@ExportedBean(defaultVisibility = 999)
@Extension
public class LeaderBoardAction implements RootAction, AccessControlled {

    private static final long serialVersionUID = 1L;

    public String getDisplayName() {
        return Messages.Leaderboard_Title();
    }

    public String getIconFileName() {
        return GameDescriptor.ACTION_LOGO_MEDIUM;
    }

    public String getUrlName() {
        return "/cigame"; //$NON-NLS-1$
    }

    /**
     * Returns the user that are participants in the ci game
     * 
     * @return list containing users.
     */
    @Exported
    public List<UserScore> getUserScores() {
        return getUserScores(User.getAll(), Hudson.getInstance().getDescriptorByType(GameDescriptor.class).getNamesAreCaseSensitive());
    }
    
    @Exported
    public boolean isUserAvatarSupported() {
        return new VersionNumber(Hudson.VERSION).isNewerThan(new VersionNumber("1.433"));
    }

    List<UserScore> getUserScores(Collection<User> users, boolean usernameIsCasesensitive) {
        ArrayList<UserScore> list = new ArrayList<UserScore>();

        Collection<User> players;
        if (usernameIsCasesensitive) {
            players = users;
        } else {
            List<User> playerList = new ArrayList<User>();
            CaseInsensitiveUserIdComparator caseInsensitiveUserIdComparator = new CaseInsensitiveUserIdComparator();
            for (User user : users) {
                if (Collections.binarySearch(playerList, user, caseInsensitiveUserIdComparator) < 0) {
                    playerList.add(user);
                }
            }
            players = playerList;
        }
        
        for (User user : players) {
            UserScoreProperty property = user.getProperty(UserScoreProperty.class);
            if ((property != null) && property.isParticipatingInGame()) {
                list.add(new UserScore(user, property.getScore(), user.getDescription()));
            }
        }

        Collections.sort(list, new Comparator<UserScore>() {
            public int compare(UserScore o1, UserScore o2) {
                if (o1.score < o2.score)
                    return 1;
                if (o1.score > o2.score)
                    return -1;
                return 0;
            }
        });

        return list;
    }

    public void doResetScores( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        if (Hudson.getInstance().getACL().hasPermission(Hudson.ADMINISTER)) {
            doResetScores(User.getAll());
        }
        rsp.sendRedirect2(req.getContextPath());
    }

    void doResetScores(Collection<User> users) throws IOException {
        for (User user : users) {
            UserScoreProperty property = user.getProperty(UserScoreProperty.class);
            if (property != null) {
                property.setScore(0);
                user.save();
            }
        }
    }

    public  void doBackUpLeaderBoard(StaplerRequest req, StaplerResponse rsp ) throws IOException{
        if (Jenkins.getInstance().getACL().hasPermission(Hudson.ADMINISTER)) {
            doBackUpLeaderBoard(User.getAll());
        }
        rsp.sendRedirect2(req.getContextPath());
    }

    void  doBackUpLeaderBoard(Collection<User> participants)  throws IOException{
        String documentName = "Leaderboard back-up " + new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        try{
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.newDocument();
            Element rootElement = document.createElement("backup");
            document.appendChild(rootElement);

            for(User participant: participants) {

                Element user = document.createElement("user");
                user.appendChild(document.createTextNode(participant.getDisplayName()));
                rootElement.appendChild(user);

                Element score = document.createElement("score");
                String scoreValue = "0.0";
                UserScoreProperty property = participant.getProperty(UserScoreProperty.class);
                if ((property != null) && property.isParticipatingInGame()) {
                    scoreValue = Double.toString(property.getScore());
                }
                score.appendChild(document.createTextNode(scoreValue));
                user.appendChild(score);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(document);
            File backupFile = new File("C:\\log\\"+documentName+".xml");
            backupFile.createNewFile();
            StreamResult result = new StreamResult(backupFile);

            transformer.transform(source,result);


        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    @ExportedBean(defaultVisibility = 999)
    public class UserScore {
        private User user;
        private double score;
        private String description;

        public UserScore(User user, double score, String description) {
            super();
            this.user = user;
            this.score = score;
            this.description = description;
        }

        @Exported
        public User getUser() {
            return user;
        }

        @Exported
        public double getScore() {
            return score;
        }

        @Exported
        public String getDescription() {
            return description;
        }
    }

    public ACL getACL() {
        return Hudson.getInstance().getACL();
    }

    public void checkPermission(Permission p) {
        getACL().checkPermission(p);
    }

    public boolean hasPermission(Permission p) {
        return getACL().hasPermission(p);
    }

    public Api getApi() {
        return  new Api(this);
    }


}
