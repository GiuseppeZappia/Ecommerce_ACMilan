import 'package:flutter/material.dart';
import 'package:frontend/UI/HomePage.dart';
import 'package:frontend/UI/OrderDetailsPage.dart';
import 'package:intl/intl.dart';
import 'package:frontend/model/Model.dart';
import 'package:frontend/model/objects/Ordine.dart';

class OrdersPage extends StatefulWidget {
  @override
  _OrdersPageState createState() => _OrdersPageState();
}

class _OrdersPageState extends State<OrdersPage> {
  DateTime? _startDate;
  DateTime? _endDate;
  Future<List<Ordine>?>? _ordersFuture;

  @override
  void initState() {
    super.initState();
    _fetchOrders();
  }

  void _fetchOrders() {
    setState(() {
      if (_startDate != null && _endDate != null) {
        _ordersFuture = Model.sharedInstance.elencoOrdiniNelPeriodo(
            _startDate!,
            _endDate!,
            0,
            20,
            "data"
        );
      } else {
        _ordersFuture = Model.sharedInstance.elencoOrdiniUtente(0, 20, "data");
      }
    });
  }

  Future<void> _selectStartDate(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _startDate ?? DateTime.now(),
      firstDate: DateTime(2000),
      lastDate: DateTime(2101),
      builder: (context, child) {
        return Theme(
          data: ThemeData.light().copyWith(
            primaryColor: Colors.blueGrey,
            buttonTheme: ButtonThemeData(textTheme: ButtonTextTheme.primary),
          ),
          child: child!,
        );
      },
    );
    if (picked != null && picked != _startDate) {
      setState(() {
        _startDate = picked;
      });
      _fetchOrders();
    }
  }

  Future<void> _selectEndDate(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _endDate ?? DateTime.now(),
      firstDate: DateTime(2000),
      lastDate: DateTime(2101),
      builder: (context, child) {
        return Theme(
          data: ThemeData.light().copyWith(
            primaryColor: Colors.blueGrey,
            //: Colors.deepOrange,
            buttonTheme: ButtonThemeData(textTheme: ButtonTextTheme.primary),
          ),
          child: child!,
        );
      },
    );
    if (picked != null && picked != _endDate) {
      setState(() {
        _endDate = picked;
      });
      _fetchOrders();
    }
  }

  Future<void> _removeOrder(BuildContext context, int orderId, DateTime orderDate) async {
    final result = await Model.sharedInstance.rimozioneOrdine(orderId);

    if (result == null) {
      final oneHourAgo = DateTime.now().subtract(Duration(hours: 1));
      final message = orderDate.isBefore(oneHourAgo)
          ? "Ordine non più annullabile, è passata più di un'ora da ${DateFormat('dd-MM-yyyy HH:mm').format(orderDate)}"
          : "Errore durante il rimborso dell'ordine, i punti derivanti da questo ordine sono stati utilizzati per un altro, per annullare questo ordine procedere con il rimborso prima del più recente se possibile.";
      _showDialog(context, "Errore", message);
    } else {
      _showDialog(context, "Successo", "Rimborso avvenuto con successo", redirectToHome: true);
    }
  }

  void _showDialog(BuildContext context, String title, String content, {bool redirectToHome = false}) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Text(content),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              if (redirectToHome) {
                Navigator.of(context).pop();
                Navigator.pushAndRemoveUntil(
                  context,
                  MaterialPageRoute(builder: (context) => HomePage()),
                      (Route<dynamic> route) => false,
                );
              }
            },
            child: Text('OK'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
        backgroundColor: Colors.blueGrey.shade300,
        title: Text('Ordini'),
        elevation: 0,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: <Widget>[
                ElevatedButton(
                  onPressed: () => _selectStartDate(context),
                  child: Text(
                    _startDate == null
                        ? 'Data Inizio'
                        : DateFormat('dd-MM-yyyy').format(_startDate!),
                  style: TextStyle(color: Colors.black),),
                ),
                ElevatedButton(
                  onPressed: () => _selectEndDate(context),
                  child: Text(
                    _endDate == null
                        ? 'Data Fine'
                        : DateFormat('dd-MM-yyyy').format(_endDate!),
                  style: TextStyle(color: Colors.black),),
                ),
              ],
            ),
            SizedBox(height: 16.0),
            Expanded(
              child: FutureBuilder<List<Ordine>?>(
                future: _ordersFuture,
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return Center(child: CircularProgressIndicator());
                  } else if (snapshot.hasError) {
                    return Center(child: Text('Errore nel caricamento degli ordini'));
                  } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                    return Center(child: Text('Nessun ordine trovato'));
                  } else {
                    return ListView.builder(
                      itemCount: snapshot.data!.length,
                      itemBuilder: (context, index) {
                        Ordine ordine = snapshot.data![index];
                        return Card(
                          margin: EdgeInsets.symmetric(vertical: 8.0),
                          elevation: 4,
                          child: ListTile(
                            contentPadding: EdgeInsets.all(12.0),
                            title: Text('Ordine #${ordine.id}', style: TextStyle(fontWeight: FontWeight.bold)),
                            subtitle: Text('Data: ${DateFormat('dd-MM-yyyy HH:mm').format(ordine.data!)}'),
                            trailing: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Text(
                                  'Totale: ${ordine.totale}€',
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    color: Colors.green,
                                    fontSize: 17,
                                  ),
                                ),
                                IconButton(
                                  icon: Icon(Icons.delete, color: Colors.red),
                                  onPressed: () => showDialog(
                                    context: context,
                                    builder: (context) => AlertDialog(
                                      title: Text("Conferma richiesta rimborso ordine"),
                                      content: Text("Sei sicuro di voler richiedere il rimborso per l'ordine selezionato?"),
                                      actions: [
                                        TextButton(
                                          onPressed: () {
                                            _removeOrder(context, ordine.id, ordine.data);
                                            Navigator.pop(context);
                                          },
                                          child: Text('OK'),
                                        ),
                                        TextButton(
                                          onPressed: () => Navigator.pop(context),
                                          child: Text("Annulla"),
                                        ),
                                      ],
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            onTap: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) => OrderDetailsPage(orderId: ordine.id),
                                ),
                              );
                            },
                          ),
                        );
                      },
                    );
                  }
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
