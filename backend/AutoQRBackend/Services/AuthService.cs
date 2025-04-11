using System.Net.Http.Json;
using AutoQRBackend.DTOs;
using AutoQRBackend.Models;
using Google.Cloud.Firestore;
using Microsoft.Extensions.Configuration;
using Newtonsoft.Json;

namespace AutoQRBackend.Services;

public class AuthService
{
	private readonly HttpClient _http;
	private readonly FirestoreDb _firestoreDb;
	private readonly string _apiKey;

	public AuthService(IConfiguration config)
	{
		_http = new HttpClient();
		_apiKey = config["Firebase:ApiKey"];

	
		var path = "autoqr-4e823-firebase-adminsdk-fbsvc-bf73ebb448.json";
		Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", path);

		_firestoreDb = FirestoreDb.Create(config["Firebase:ProjectId"]);
	}

	public async Task<string?> RegisterAsync(RegisterRequest req)
	{

		var query = _firestoreDb.Collection("users")
			 .WhereEqualTo("Username", req.Username);
		var snapshot = await query.GetSnapshotAsync();

		if (snapshot.Count > 0)
			return "Username already exists";

		var payload = new
		{
			email = req.Email,
			password = req.Password,
			returnSecureToken = true
		};

		var response = await _http.PostAsJsonAsync(
			 $"https://identitytoolkit.googleapis.com/v1/accounts:signUp?key={_apiKey}",
			 payload);

		if (!response.IsSuccessStatusCode)
			return "Firebase Auth failed";

		var json = await response.Content.ReadAsStringAsync();
		var obj = JsonConvert.DeserializeObject<dynamic>(json);
		string uid = obj.localId;

		var user = new UserModel
		{
			Email = req.Email,
			Username = req.Username,
			QrCode = $"autoqr:{req.Username}",
			ProfileImageUrl = "https://i.ibb.co/0j1XgT6/default-avatar.png"
		};

		await _firestoreDb.Collection("users").Document(uid).SetAsync(user);
		return null;
	}

	public async Task<string?> LoginAsync(LoginRequest req)
	{
		var payload = new
		{
			email = req.Email,
			password = req.Password,
			returnSecureToken = true
		};

		var response = await _http.PostAsJsonAsync(
			 $"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={_apiKey}",
			 payload);

		if (!response.IsSuccessStatusCode)
			return null;

		var json = await response.Content.ReadAsStringAsync();
		var obj = JsonConvert.DeserializeObject<dynamic>(json);
		return obj.idToken;
	}
}
