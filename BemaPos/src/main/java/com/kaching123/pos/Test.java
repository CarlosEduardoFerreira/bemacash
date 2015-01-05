package com.kaching123.pos;

import java.io.IOException;

import com.kaching123.pos.printer.BarcodeAction;
import com.kaching123.pos.printer.BarcodeHeightAction;
import com.kaching123.pos.printer.BarcodeTextBelowPositionAction;
import com.kaching123.pos.printer.CenterAlignment;
import com.kaching123.pos.printer.InitPrintAction;
import com.kaching123.pos.printer.PrintLineAction;

public class Test {

	public static void main(String[] args) throws IOException {
		/*Socket socket = new Socket();
		socket.connect(new InetSocketAddress("192.168.0.107", 9100));
		PosPrinter printer = new PosPrinter(socket);
		printer.init();
		printer.openDrawer();
		System.out.println(printer.getPrinterName());
		printer.close();*/
		
		SocketPrinter printer = new SocketPrinter("192.168.178.157", 9100);
		/*String printerName = new GetPrinterNameAction().execute(printer);
		System.out.println("printer name: " + printerName);
		new InitPrintAction().execute(printer);
        PrinterStatusEx status = new GetPrinterStatusExAction(1).execute(printer);
        System.out.println("printer status: noPaper = " + status.offlineStatus.noPaper);
        System.out.println("printer status: paperIsNearEnd = " + status.offlineStatus.paperIsNearEnd);
        System.out.println("printer status: isBusy = " + status.printerStatus.isBusy);
        System.out.println("printer status: printerIsOffline = " + status.printerStatus.printerIsOffline);*/

		//new AutomaticLineFeedAction(true).execute(printer);
		
		//new PrintLineAction(new Date() + " The paper feed amount set by this command does not affect the values set by ESC 2 or ESC 3.").execute(printer);
		/*new PrintAndPaperFeedAction().execute(printer);
		new PrintAndPaperFeedAction().execute(printer);*/

        new InitPrintAction().execute(printer);
        new BarcodeTextBelowPositionAction().execute(printer);
        new BarcodeHeightAction(96).execute(printer);
        new CenterAlignment().execute(printer);
        new BarcodeAction("123-123").execute(printer);
        new PrintLineAction("som text here").execute(printer);
        /*new PrintLineAction("").execute(printer);
        new PrintLineAction("").execute(printer);
        new PrintLineAction("").execute(printer);*/
		//new FullPaperCutAction().execute(printer);
		/*for(int i = 0; i < 10; i++){
		}*/
        //new FullPaperCutAction2().execute(printer);
		printer.close();
	}
}
