package net.sourcewalker.smugview.parcel;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginResult implements Parcelable {

    private String username;
    private int id;
    private String accountType;
    private String session;
    private boolean successful;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    private LoginResult(Parcel source) {
        username = source.readString();
        id = source.readInt();
        accountType = source.readString();
        session = source.readString();
        successful = source.readInt() == 1;
    }

    public LoginResult() {
        successful = false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeInt(id);
        dest.writeString(accountType);
        dest.writeString(session);
        dest.writeInt(successful ? 1 : 0);
    }

    public static final Parcelable.Creator<LoginResult> CREATOR = new Creator<LoginResult>() {

        @Override
        public LoginResult[] newArray(int size) {
            return new LoginResult[size];
        }

        @Override
        public LoginResult createFromParcel(Parcel source) {
            return new LoginResult(source);
        }
    };

}
