package cs.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import hxc.ecds.protocol.rest.Violation;
import lombok.AllArgsConstructor;
import lombok.Data;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment=WebEnvironment.MOCK)
public class TypeConvertorServiceTest
{
	@Autowired
	@SuppressWarnings("unused") // too many potential side effects if removed
	private TypeConvertorService typeConvertorService;

	@Data
	@AllArgsConstructor(staticName = "of")
	public static class Pair<FirstType, SecondType> {
		private FirstType first;
		private SecondType second;
	}

	@Test
	public void testUtil()
	{
		{
			List< String > invalidStrings = Arrays.asList(
				"ZZ:ZZ:ZZ",
				"24:00:00",
				"-1:-1:-1",
				"ZZ:00:00",
				"00:ZZ:00",
				"00:00:ZZ"
			);
			for ( String invalidString : invalidStrings )
			{
				List<Violation> violations = new ArrayList< Violation >();
				Integer seconds = TypeConvertorService.timeOfDayStringToSeconds( invalidString, "foo", violations );
				System.err.printf("Checking %s -> %s == null ...\n", invalidString, seconds);
				assertNull(seconds);
				assertTrue( violations.size() > 0 );
			}
			List< Pair< String, Integer > > validStrings = Arrays.asList(
				new Pair< String, Integer >( "00:00:00",  0 * 60 * 60 +  0 * 60 +  0 ),
				new Pair< String, Integer >( "23:59:59", 23 * 60 * 60 + 59 * 60 + 59 ),
				new Pair< String, Integer >( "02:00:59",  2 * 60 * 60 +  0 * 60 + 59 ),
				new Pair< String, Integer >( "12:13:14", 12 * 60 * 60 + 13 * 60 + 14 )
			);
			for ( Pair< String, Integer > validString : validStrings )
			{
				List<Violation> violations = new ArrayList< Violation >();
				Integer seconds = TypeConvertorService.timeOfDayStringToSeconds( validString.getFirst(), "foo", violations );
				System.err.printf("Checking %s -> %s == %s ...\n", validString.getFirst(), validString.getSecond(), seconds);
				assertNotNull( seconds );
				assertTrue( violations.size() == 0 );
				assertEquals( validString.getSecond(), seconds );
			}
		}
	}
}
