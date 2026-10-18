
/// values for calculating spam/ ham probability
static double alpha = 0.5;
static double threshold = 0.5;

/// amount of training spam and ham mails
static int countSpamTrainingData = 0;
static int countHamTrainingData = 0;

/// map which holds the percentage values of the training data and the words
HashMap<String, MailWord> analysis = new HashMap<>();


void main() {

    // header of the output
    System.out.println("Spamfilter");
    System.out.println("***********");
    System.out.println();

    // calibrate the alpha dn threshold values
    doCalibration();

    // test the calibrated values on the test zips
    System.out.println("Testing");
    doTest();
}

void doTest(){
    String path = getClass().getResource("ham-test.zip").getPath();
    var data = loadWords(path);
    int failedHams = 0;
    for(var d: data){
        var res = calulateProbability(d);
        // check if it has been classified correctly
        if(res >= threshold)failedHams++;
    }
    System.out.println("Ham Mails Test | Total Mails: "+data.size()+"; Failed Mails: "+failedHams
            +"; korrekt: "+(100.0 - 100.0 * failedHams / data.size())+" %");

    path = getClass().getResource("spam-test.zip").getPath();
     data = loadWords(path);
    int failedSpams = 0;
    for(var d: data){
        var res = calulateProbability(d);
        // check if it has been classified correctly
        if(res <= threshold) failedSpams++;
    }
    System.out.println("Spam Mails Test | Total Mails: "+data.size()+"; Failed Mails: "+failedSpams
            +"; korrekt: "+(100.0 - 100.0 * failedSpams / data.size())+" %");
}


void doCalibration(){
    doTraining();

    List<CalibrationResult> results = new ArrayList<>();

    System.out.println();
    System.out.println("Calibration");
    String path = getClass().getResource("ham-kallibrierung.zip").getPath();
    var hamData = loadWords(path);
    path = getClass().getResource("spam-kallibrierung.zip").getPath();
    var spamData = loadWords(path);

    // looping through all the alpha and threshold values
    double loopAlpha = 0.5;
    while(loopAlpha > 0.000000000000000000000001){

        double loopThreshold = 0.1;
        while(loopThreshold < 1){

            alpha = loopAlpha;
            threshold = loopThreshold;

            int failedHams = 0;
            for(var d: hamData){
                var res = calulateProbability(d);
                if(res > threshold)failedHams++;
            }

            int failedSpams = 0;
            for(var d: spamData){
                var res = calulateProbability(d);
                if(res <= threshold) failedSpams++;
            }

            CalibrationResult result = new CalibrationResult(loopAlpha, loopThreshold,
                    (double) failedSpams/spamData.size(), (double) failedHams/hamData.size());
            results.add(result);

            loopThreshold = loopThreshold + 0.1;
        }

        loopAlpha = loopAlpha / 10;
    }

    // get the best alpha and threshold result
    var bli = results.stream()
            .min(Comparator.comparingDouble(r -> r.getFailureRatHam() + r.getFailureRateSpam()))
            .get();

    alpha = bli.getAlpha();
    threshold = bli.getThreshold();

    System.out.println("alpha: "+bli.getAlpha()+" threshold: "+bli.getThreshold()
            +" ham failure "+bli.getFailureRatHam()+" spam failure "+bli.getFailureRateSpam());
    System.out.println();
}

void doTraining(){
    System.out.println("Reading Training Data");

    String path = getClass().getResource("ham-anlern.zip").getPath();
    var data = loadWords(path);
    countHamTrainingData = data.size();
    for(var d : data){
        for(var w : d._words){
            addtoHam(w);
        }
    }

    path = getClass().getResource("spam-anlern.zip").getPath();
    data = loadWords(path);
    countSpamTrainingData = data.size();
    for(var d : data){
        for(var w : d._words){
            addToSpam(w);
        }
    }

    // calculate the actual percentage out of spam and ham counts
    for(var s : analysis.keySet()){
        analysis.get(s).setPercentage();
    }
}

//b) function for calculating spam probability
// it caluclates the percentage of the spam probability directly
double calulateProbability(EmailContent content){
    double q = 1;
    boolean found = false;

    for (var word : content._words) {
        var m = analysis.get(word.toLowerCase());
        if (m == null) continue;
        q *= m.get_hamProbability() /  m.get_spamProbability();
        found = true;
    }

    if (!found) return 0.5;
    return  1 / (q + 1);
}



/// load all the data from one zip into a list. but all words of one mail are only counted one
List<EmailContent> loadWords(String filename){

    ArrayList<EmailContent> emails = new ArrayList<>();
    try (ZipFile zipFile = new ZipFile(filename)) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {

                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                    String content = s.hasNext() ? s.next() : "";

                    String[] rawwords = content.trim().split("\\s+");

                    // count the words only once
                    Set<String> mySet = new HashSet<>();
                    for (var w : rawwords) {
                        mySet.add(w.toLowerCase());
                    }

                    EmailContent email = new EmailContent(entry.getName(), mySet.stream().toArray(String[] ::new));
                    emails.add(email);
                }
            }
        }
    }catch (IOException ex){
        System.err.println("failed open ZIP "+filename+ex.getMessage());
    }
    return  emails;
}

void addToSpam(String word){
    word = word.toLowerCase().trim();
    if(analysis.containsKey(word)){
        analysis.get(word).addSpam();
    }else{
        MailWord counter = new MailWord();
        counter.addSpam();
        analysis.put(word, counter);
    }
}

void addtoHam(String word){
    word = word.toLowerCase().trim();
    if(analysis.containsKey(word)){
        analysis.get(word).addHam();
    }else{
        MailWord counter = new MailWord();
        counter.addHam();
        analysis.put(word, counter);
    }
}

/// Class to keep track of each calibration result (in the calibration method)
class CalibrationResult{

    private double alpha;

    private double failureRateSpam;

    private double failureRatHam;
    private double threshold;

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public CalibrationResult(double alpha, double threshold, double failureRateSpam, double failureRatHam) {
        this.alpha = alpha;
        this.threshold = threshold;
        this.failureRateSpam = failureRateSpam;
        this.failureRatHam = failureRatHam;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getFailureRateSpam() {
        return failureRateSpam;
    }

    public void setFailureRateSpam(double failureRateSpam) {
        this.failureRateSpam = failureRateSpam;
    }

    public double getFailureRatHam() {
        return failureRatHam;
    }

    public void setFailureRatHam(double failureRatHam) {
        this.failureRatHam = failureRatHam;
    }
}

/// represents the content of one email (words are only represented once)
class EmailContent{
    String _filename = "";
    String[] _words;

    public EmailContent(String _filename, String[] _words) {
        this._filename = _filename;
        this._words = _words;
    }
}

/// represents the word count, as well as the percentage
class MailWord{

    public void addSpam(){
        _spamAmount++;
    }
    public void addHam(){
        _hamAmount++;
    }
    public void setPercentage(){
       if(countSpamTrainingData != 0 && countHamTrainingData !=0){
           _spamProbability =_spamAmount / countSpamTrainingData;
           _hamProbability = _hamAmount / countHamTrainingData;


       }
    }


    public double get_spamAmount() {
        return _spamAmount;
    }

    public void set_spamAmount(double _spamAmount) {
        this._spamAmount = _spamAmount;
    }

    public double get_hamAmount() {
        return _hamAmount;
    }

    public void set_hamAmount(double _hamAmount) {
        this._hamAmount = _hamAmount;
    }

    public double get_spamProbability() {
        return _spamProbability != 0 ? _spamProbability : alpha;
    }

    public double get_hamProbability() {
        return _hamProbability != 0 ? _hamProbability : alpha;
    }

    private double _spamProbability = 0;
    private double _hamProbability = 0;
    private double _spamAmount = 0;
    private double _hamAmount = 0;


}

