package optionsParser.parsing

import optionsParser.OptionsMinMaxPricesFinder
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentMap


class OptionsMinMaxPriceFinderTest extends Specification {
    
    def "test min and max prices for contracts"() {
        
        given: "three csv files in one directory"
        Path directoryWithTestData = Paths.get(getClass().getResource('/integrationTestData/').getPath())
                
        when: "min/max price finding logic initialized with the target directory"
        def pricesFinder = new OptionsMinMaxPricesFinder()

        and: "calculation launched and returned results"
        ConcurrentMap<OptionByteBuffer,int[]> results = pricesFinder.getMinAndMaxPricesInContracts(directoryWithTestData)
        
        then: "no exceptions in threads"
        pricesFinder.communication.ticksParsers.each {it.uncaughtExceptionHandler == null }
        
        and: "computed results equals to expected"
        results.size() == 7
        OptionByteBuffer tempBuffer = new OptionByteBuffer()
        results.get(tempBuffer.setSingleTick('ADBE,C,20200117,770000   '.getBytes())) == [0,0]
        results.get(tempBuffer.setSingleTick('ACWI,P,20200117,610000   '.getBytes())) == [0,555]
        results.get(tempBuffer.setSingleTick('ACWI,P,20200117,810000   '.getBytes())) == [0,555]
        results.get(tempBuffer.setSingleTick('ACWI,P,20200117,770000   '.getBytes())) == [0,0]
        results.get(tempBuffer.setSingleTick('ACWI,C,20200117,770000   '.getBytes())) == [0, 9999999]
        results.get(tempBuffer.setSingleTick('ACWI,P,20200117,650000   '.getBytes())) == [0, 555]
        results.get(tempBuffer.setSingleTick('ADBE,C,20200117,1450000  '.getBytes())) == [0, 555]
    }
    
}
