

#class_name="BaeldungTest#whenRequestingChunks10_thenMessagesAreReceived"
#class_name="BaeldungTest#whenLimitRateSet_thenSplitIntoChunks"
class_name="BaeldungTest#whenCancel_thenSubscriptionFinished"
mvn -Dtest=com.example.gly_gateway.${class_name} test
