package zemberek.lm;

/**
 * Represents an N-gram language model.
 */
public interface NgramLanguageModel {

    /**
     * Returns Log uni-gram probability value. id must be in vocabulary limits.
     * @param id word id
     * @return log probability
     */
    double getUnigramProbability(int id);

    /**
     * Returns Log N-Gram probability.
     * If this is a back-off model, it makes with necessary back-off calculations when necessary
     * @param ids word ids.
     * @return log probability
     */
    double getProbability(int... ids);

    /**
     * Returns Log 3-Gram probability.
     * If this is a back-off model, it makes with necessary back-off calculations when necessary
     */
    double getTriGramProbability(int id0, int id1, int id2);

    /**
     * Returns Log 3-Gram probability.
     * If this is a back-off model, it makes with necessary back-off calculations when necessary
     */
    double getTriGramProbability(int id0, int id1, int id2, int fingerPrint);

    /**
     * Order of language model
     * @return order value. 1,2,.3 typically.
     */
    int getOrder();

    /**
     * Vocabulary of this model.
     * @return Vocabulary of this model.
     */
    LmVocabulary getVocabulary();
}
