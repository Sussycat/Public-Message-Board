package WebServer;

import java.util.Date;

public class Message {
    public int MID;
    public int UID;
    public int GID;
    public String Sub = "";
    public String Body = "";
    public Date Pdate;
    public Message(int MeID, int UsID ,int GrID,String S, String Bd, Date Idate){
        this.MID = MeID;
        this.UID = UsID;
        this.GID = GrID;
        this.Sub = S;
        this.Body = Bd;
        this.Pdate = Idate;
    }
}