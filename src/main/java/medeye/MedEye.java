package medeye;

import medeye.imaging.ImageUtil;
import medeye.medical.DrugSimilarity;
import medeye.medical.DrugUtil;
import medeye.wrapper.ActiveSideEffectWrapper;
import medeye.wrapper.AlternativePrices;
import medeye.wrapper.WrapperUtil;

import java.io.IOException;

public class MedEye {
    // init drug database for basic information
    private static final String DRUG_NAME_DATABASE_URL = "https://data.medicaid.gov/resource/tau9-gfwr.json";
    public static final DrugUtil.DrugInfo[] DRUG_DATABASE = Utility.gson.fromJson(Utility.getStringFromUrl(DRUG_NAME_DATABASE_URL), DrugUtil.DrugInfo[].class);

    // const for base image directory
    private static final String BASE_IMAGE_DIR = "MedEye_Images/";

    public static void main(String[] args) throws IOException {
        // Sets path to the image file to annotate
        String fileName = BASE_IMAGE_DIR + "nap.png";

        // Runs Optical Character Recognition (OCR) on the image file to determine the target drug name
        String targetDrugName = Utility.omitLastNewline(ImageUtil.runOCR(fileName)).toUpperCase();

        System.out.println("TARGET: " + targetDrugName); // debug testing

        Utility.padding(3, "Processing Drugs");
        // Processes DRUG_DATABASE by applying filters, then sorting by increasing unit price
        DrugUtil.processDrugs(DRUG_DATABASE, targetDrugName)
                .forEach(drug -> System.out.println(drug.getName() + "\n$" + drug.getUnitPrice() + " / " + drug.getUnit()));

        // Active ingredients and side effects (works)
        ActiveSideEffectWrapper activeSideEffectWrapper = WrapperUtil.getActiveSideEffects(targetDrugName);

        // 3 column Name : Alternative : Similarity
        AlternativePrices alternativePrices = new AlternativePrices(targetDrugName);

        Utility.padding(3, "Similar Drugs");
        // Outputs top 10 similar DRUG_DATABASE (that isn't itself)
        DrugSimilarity.getSimilar(targetDrugName)
                .forEach(p -> System.out.println("Name: " + Utility.properCapital(p.getKey()) + "\tScore: " + p.getValue()));

        Utility.padding(3, "Ingredients");
        DrugUtil.getIngredients(DrugUtil.getRxcuiFromCommon(targetDrugName))
                .forEach(System.out::println);
    }
}