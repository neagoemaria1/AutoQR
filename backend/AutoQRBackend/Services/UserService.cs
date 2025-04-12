using Google.Cloud.Firestore;
using AutoQRBackend.Models;
using FirebaseAdmin.Messaging;


namespace AutoQRBackend.Services
{
	public class UserService
	{
		private readonly FirestoreDb _firestoreDb;

		public UserService(IConfiguration config)
		{
			_firestoreDb = FirestoreDb.Create(config["Firebase:ProjectId"]);

		}

		public async Task<UserModel?> GetUserByQrCodeAsync(string qrCode)
		{
			var query = _firestoreDb.Collection("users")
				 .WhereEqualTo("QrCode", qrCode);

			var snapshot = await query.GetSnapshotAsync();
			if (snapshot.Count > 0)
			{
				var userDoc = snapshot.Documents.First();
				return userDoc.ConvertTo<UserModel>();
			}
			return null;
		}


		public async Task UpdateUserAsync(UserModel user, string uid)
		{
			if (user == null)
				throw new ArgumentNullException(nameof(user));
			if (string.IsNullOrEmpty(uid))
				throw new ArgumentException("UID cannot be null or empty.", nameof(uid));
			DocumentReference userDoc = _firestoreDb.Collection("users").Document(uid);

			await userDoc.SetAsync(user);
		}


		public async Task<List<UserModel>> GetAllUsersAsync()
		{
			var snapshot = await _firestoreDb.Collection("users").GetSnapshotAsync();
			return snapshot.Documents.Select(doc => doc.ConvertTo<UserModel>()).ToList();
		}

		public async Task<List<Dictionary<string, object>>> GetInboxMessagesAsync(string uid)
		{
			var messages = new List<Dictionary<string, object>>();
			var inboxRef = _firestoreDb.Collection("users").Document(uid).Collection("inbox");
			var snapshot = await inboxRef.OrderByDescending("timestamp").GetSnapshotAsync();

			foreach (var doc in snapshot.Documents)
			{
				var data = doc.ToDictionary();

				if (data.TryGetValue("timestamp", out var tsObj) && tsObj is Timestamp ts)
				{
					var unixTimeSeconds = ((DateTimeOffset)ts.ToDateTime()).ToUnixTimeSeconds();
					data["timestamp"] = new Dictionary<string, object>
					{
						["seconds"] = unixTimeSeconds
					};
				}


				messages.Add(data);
			}

			return messages;
		}


	}
}
