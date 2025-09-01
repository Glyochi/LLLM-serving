

#class_name="BaeldungTest#whenRequestingChunks10_thenMessagesAreReceived"
#class_name="BaeldungTest#whenLimitRateSet_thenSplitIntoChunks"
class_name="BaeldungTest#whenCancel_thenSubscriptionFinished"
mvn -Dtest=com.glygateway.${class_name} test
