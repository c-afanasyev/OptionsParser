package optionsParser.parsing

import optionsParser.multithreadSettings.InterThreadCommunication
import spock.lang.Specification


class OptionsMinMaxPriceFinderTest extends Specification {
    
    def "test min and max prices for contracts"() {
        
        given: "three csv files"
        List<String> file1 = new File(getClass().getResource('/optionsTicksTestData1.csv').getFile()) as String[]
        List<String> file2 = new File(getClass().getResource('/optionsTicksTestData2.csv').getFile()) as String[]
        List<String> file3 = new File(getClass().getResource('/optionsTicksTestData3.csv').getFile()) as String[]
                
        when: "min/max price finding logic initialized with lines of data"
        def threadingSettings = new InterThreadCommunication()
        Thread[] threads = [new Thread(new OptionsMinMaxPriceFinder(file1, threadingSettings)),
                            new Thread(new OptionsMinMaxPriceFinder(file2, threadingSettings)),
                            new Thread(new OptionsMinMaxPriceFinder(file3, threadingSettings))]
        
        and: "all threads launched"
        threads.each {it.start()}
        
        and: "waited till threads are done and shutdown executors"
        threads.each {it.join()}
        threadingSettings.shutDownExecutors()
        
        then: "no exceptions in threads"
        threads*.uncaughtExceptionHandler == [null,null,null]
        
        and: "computed results equals to expected"
        def results = threadingSettings.combinedResults
        results.size() == 7
        results.get('ADBE,C,20200117,770000') == [0,0]
        results.get('ACWI,P,20200117,610000') == [0,555]
        results.get('ACWI,P,20200117,810000') == [0,555]
        results.get('ACWI,P,20200117,770000') == [0,0]
        results.get('ACWI,C,20200117,770000') == [-1111111, 9999999]
        results.get('ACWI,P,20200117,650000') == [0, 555]
        results.get('ADBE,C,20200117,1450000') == [0, 555]
    }
    
}
