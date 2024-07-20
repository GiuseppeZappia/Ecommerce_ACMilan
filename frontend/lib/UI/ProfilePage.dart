import 'package:flutter/material.dart';
import 'package:frontend/model/Model.dart';
import 'package:frontend/model/objects/Utente.dart';
import 'package:frontend/model/DTO/UtenteRegistrDTO.dart';
import 'package:frontend/model/support/LoginResult.dart';

class ProfilePage extends StatefulWidget {
  @override
  _ProfilePageState createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  Utente? _loggedUser;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _checkLoggedUser();
  }

  Future<void> _checkLoggedUser() async {
    Utente? user = await Model.sharedInstance.getLoggedUser();
    setState(() {
      _loggedUser = user;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Profilo Utente'),
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : _loggedUser != null
          ? _buildUserProfile()
          : _buildLoginRegister(),
    );
  }

  Widget _buildUserProfile() {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Nome: ${_loggedUser!.nome}', style: TextStyle(fontSize: 18)),
          SizedBox(height: 8),
          Text('Cognome: ${_loggedUser!.cognome}', style: TextStyle(fontSize: 18)),
          SizedBox(height: 8),
          Text('Email: ${_loggedUser!.email}', style: TextStyle(fontSize: 18)),
          SizedBox(height: 8),
          Text('Punti Fedeltà: ${_loggedUser!.puntifedelta}', style: TextStyle(fontSize: 18)),
          SizedBox(height: 24),
          ElevatedButton(
            onPressed: () async {
              await Model.sharedInstance.logOut();
              setState(() {
                _loggedUser = null;
              });
            },
            child: Text('Logout'),
          ),
        ],
      ),
    );
  }

  Widget _buildLoginRegister() {
    final _formKey = GlobalKey<FormState>();
    TextEditingController emailController = TextEditingController();
    TextEditingController passwordController = TextEditingController();

    return Center(
      child: Padding(
      padding: const EdgeInsets.only(top:50.0),//const EdgeInsets.all(16.0),

      child:SizedBox(
        width: 300,
        child:
        Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TextFormField(
              controller: emailController,
              decoration: InputDecoration(labelText: 'Email'),
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Inserisci una email';
                }
                return null;
              },

            ),
            SizedBox(height: 8),
            TextFormField(
              controller: passwordController,
              decoration: InputDecoration(labelText: 'Password'),
              obscureText: true,
              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Inserisci una password';
                }
                return null;
              },
            ),
            SizedBox(height: 24),
            ElevatedButton(
              onPressed: () async {
                if (_formKey.currentState!.validate()) {
                  var result = await Model.sharedInstance.logIn(
                    emailController.text,
                    passwordController.text,
                  );
                  if (result == LogInResult.logged) {
                    _checkLoggedUser();
                  } else {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('Credenziali errate o altro errore')),
                    );
                  }
                }
              },
              child: Text('Login'),
            ),
            SizedBox(height: 8),
            Center(
              child: GestureDetector(
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => RegistrationPage()),
                  );
                },
                child: Text(
                  'Non sei registrato? Registrati',
                  style: TextStyle(
                    color: Colors.deepPurple,
                    decoration: TextDecoration.underline,
                  ),
                ),
              ),
            ),
          ],
        ),
        ),
      ),
      ),
    );
  }
}


class RegistrationPage extends StatefulWidget {
  @override
  _RegistrationPageState createState() => _RegistrationPageState();
}

class _RegistrationPageState extends State<RegistrationPage> {
  final _formKey = GlobalKey<FormState>();
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _firstNameController = TextEditingController();
  final TextEditingController _lastNameController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Registrazione'),
      ),
      body: Center(
      child: Padding(
        padding: const EdgeInsets.only(top:50.0),
        child:SizedBox(
        width: 300,
        child: Form(
          key: _formKey,
          child: ListView(
            children: [
              TextFormField(
                controller: _usernameController,
                decoration: InputDecoration(labelText: 'Username'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Inserisci un username';
                  }
                  return null;
                },
              ),
              SizedBox(height: 8),
              TextFormField(
                controller: _emailController,
                decoration: InputDecoration(labelText: 'Email'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Inserisci una email';
                  }
                  return null;
                },
              ),
              SizedBox(height: 8),
              TextFormField(
                controller: _firstNameController,
                decoration: InputDecoration(labelText: 'Nome'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Inserisci il tuo nome';
                  }
                  return null;
                },
              ),
              SizedBox(height: 8),
              TextFormField(
                controller: _lastNameController,
                decoration: InputDecoration(labelText: 'Cognome'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Inserisci il tuo cognome';
                  }
                  return null;
                },
              ),
              SizedBox(height: 8),
              TextFormField(
                controller: _passwordController,
                decoration: InputDecoration(labelText: 'Password'),
                obscureText: true,
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Inserisci una password';
                  }
                  return null;
                },
              ),
              SizedBox(height: 24),
              ElevatedButton(
                onPressed: () async {
                  if (_formKey.currentState!.validate()) {
                    UtenteRegistrDTO user = UtenteRegistrDTO(
                      id: 0, // ID will be assigned by the backend
                      username: _usernameController.text,
                      email: _emailController.text,
                      firstName: _firstNameController.text,
                      lastName: _lastNameController.text,
                      password: _passwordController.text,
                    );

                    Utente? result = await Model.sharedInstance.addUser(user);

                    if (result != null) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(content: Text('Registrazione riuscita!')),
                      );
                      Navigator.pop(context); // Ritorna alla pagina di login
                    } else {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(content: Text('Registrazione fallita. Email o username già esistenti.')),
                      );
                    }
                  }
                },
                child: Text('Registrati'),
              ),
            ],
          ),
        ),
      ),),
      ),);
  }
}