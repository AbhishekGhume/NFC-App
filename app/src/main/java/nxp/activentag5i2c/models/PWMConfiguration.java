package nxp.activentag5i2c.models;

import static nxp.activentag5i2c.utils.Constants.PWM_FREQUENCY;

public class PWMConfiguration {
    private int resolution;
    private int dutyCycle;
    private int startTime;
    private int prescalar;
    private String pwmFrequency;

    /*public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PWMConfiguration createFromParcel(Parcel in) {
            return new PWMConfiguration(in);
        }

        public PWMConfiguration[] newArray(int size) {
            return new PWMConfiguration[size];
        }
    };*/

    public String getPwmFrequency() {
        return pwmFrequency;
    }

    public void setPwmFrequency() {
        switch (resolution) {
            case 3:
                switch (prescalar) {
                    case 0:
                        pwmFrequency = PWM_FREQUENCY[0];
                        break;
                    case 1:
                        pwmFrequency = PWM_FREQUENCY[1];
                        break;
                    case 2:
                        pwmFrequency = PWM_FREQUENCY[2];
                        break;
                    case 3:
                        pwmFrequency = PWM_FREQUENCY[3];
                        break;
                }
                break;
            case 2:
                switch (prescalar) {
                    case 0:
                        pwmFrequency = PWM_FREQUENCY[4];
                        break;
                    case 1:
                        pwmFrequency = PWM_FREQUENCY[5];
                        break;
                    case 2:
                        pwmFrequency = PWM_FREQUENCY[6];
                        break;
                    case 3:
                        pwmFrequency = PWM_FREQUENCY[7];
                        break;
                }
                break;
            case 1:
                switch (prescalar) {
                    case 0:
                        pwmFrequency = PWM_FREQUENCY[8];
                        break;
                    case 1:
                        pwmFrequency = PWM_FREQUENCY[9];
                        break;
                    case 2:
                        pwmFrequency = PWM_FREQUENCY[10];
                        break;
                    case 3:
                        pwmFrequency = PWM_FREQUENCY[11];
                        break;
                }
                break;
            case 0:
                switch (prescalar) {
                    case 0:
                        pwmFrequency = PWM_FREQUENCY[12];
                        break;
                    case 1:
                        pwmFrequency = PWM_FREQUENCY[13];
                        break;
                    case 2:
                        pwmFrequency = PWM_FREQUENCY[14];
                        break;
                    case 3:
                        pwmFrequency = PWM_FREQUENCY[15];
                        break;
                }
                break;
        }
    }

    public PWMConfiguration() {
        this.resolution = 0;
        this.dutyCycle = 5;
        this.startTime = 5;
        this.prescalar = 0;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public int getDutyCycle() {
        return dutyCycle;
    }

    public void setDutyCycle(int dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    public int getPrescalar() {
        return prescalar;
    }

    public void setPrescalar(int prescalar) {
        this.prescalar = prescalar;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    /*// Parcelling part
    public PWMConfiguration(Parcel in) {
        int[] data = new int[4];

        in.readIntArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.resolution = data[0];
        this.dutyCycle = data[1];
        this.startTime = data[2];
        this.prescalar = data[3];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(new int[]{this.resolution,
                this.dutyCycle,
                this.startTime,
                this.prescalar});
    }*/
}
