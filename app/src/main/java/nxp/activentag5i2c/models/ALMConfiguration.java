package nxp.activentag5i2c.models;

public class ALMConfiguration {

    private int alm_conf_00;
    private int alm_conf_01;
    private int alm_conf_02;
    private int alm_conf_03;
    private int position;
    private int mask;

    public ALMConfiguration() {
        this.alm_conf_00 = 208;
        this.alm_conf_01 = 91;
        this.alm_conf_02 = 72;
        this.alm_conf_03 = 22;
    }

    public void setAlm_conf_00(byte alm_conf_00){
        this.alm_conf_00 = alm_conf_00;
    }

    public void setAlm_conf_01(byte alm_conf_01){
        this.alm_conf_01 = alm_conf_01;
    }

    public void setAlm_conf_02(byte alm_conf_02){
        this.alm_conf_02 = alm_conf_02;
    }

    public void setAlm_conf_03(byte alm_conf_03){
        this.alm_conf_03 = alm_conf_03;
    }

    public int getrxResistorValue(){
        position = 6;
        mask = 3;
        return (this.alm_conf_00 >> position) & mask;
    }

    public int getfieldThreshold(){
        position = 3;
        mask = 7;
        return (this.alm_conf_00 >> position) & mask;
    }

    public int gettuningTopology(){
        position = 0;
        mask = 3;
        return (this.alm_conf_00 >> position) & mask;
    }

    public String getstaticPhaseOffset(){
        position = 2;
        mask = 31;
        int rcv_static_phoff = (this.alm_conf_01 >> position) & mask;
        return String.valueOf(rcv_static_phoff);
    }

    public boolean getphaseShiftEnabled(){
        position = 0;
        mask = 1;
        if (((this.alm_conf_01 >> position) & mask ) == 1){
            return true;
        }else{
            return false;
        }
    }

    public int getresistorEnabled(){
        position = 1;
        mask = 1;
        return (this.alm_conf_01 >> position) & mask;
    }

    public int gettxResistorValue(){
        position = 6;
        mask = 3;
        return (this.alm_conf_02 >> position) & mask;
    }

    public String getdampingPeriod(){
        position = 0;
        mask = 15;
        int rcv_damping_period = (this.alm_conf_02 >> position) & mask;
        return String.valueOf(rcv_damping_period);
    }

    public String getpllDelay(){
        position = 3;
        mask = 7;
        int rcv_pll_delay = (this.alm_conf_03 >> position) & mask;
        return String.valueOf(rcv_pll_delay);
    }

    public String getactiveClampDelay(){
        position = 0;
        mask = 7;
        int rcv_Clampdelay = (this.alm_conf_03 >> position) & mask;
        return String.valueOf(rcv_Clampdelay);
    }

}
