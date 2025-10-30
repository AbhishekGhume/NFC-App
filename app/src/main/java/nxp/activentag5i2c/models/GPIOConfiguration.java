package nxp.activentag5i2c.models;

public class GPIOConfiguration{
    private int direction;
    private int slewRate;
    private int padConfiguration;

    /*public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PWMConfiguration createFromParcel(Parcel in) {
            return new PWMConfiguration(in);
        }

        public PWMConfiguration[] newArray(int size) {
            return new PWMConfiguration[size];
        }
    };*/

    public GPIOConfiguration(int direction) {
        this.direction = direction;
        this.slewRate = 0;
        this.padConfiguration = 0;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getSlewRate() {
        return slewRate;
    }

    public void setSlewRate(int slewRate) {
        this.slewRate = slewRate;
    }

    public int getPadConfiguration() {
        return padConfiguration;
    }

    public void setPadConfiguration(int padConfiguration) {
        this.padConfiguration = padConfiguration;
    }

    // Parcelling part
    /*public GPIOConfiguration(Parcel in) {
        int[] data = new int[3];

        in.readIntArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.direction = data[0];
        this.slewRate = data[1];
        this.padConfiguration = data[2];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(new int[]{this.direction,
                this.slewRate,
                this.padConfiguration});
    }*/
}
