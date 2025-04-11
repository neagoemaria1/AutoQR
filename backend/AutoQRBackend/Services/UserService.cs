using Google.Cloud.Firestore;
using AutoQRBackend.Models;
using Newtonsoft.Json;

namespace AutoQRBackend.Services
{
    public class UserService
    {
        private readonly FirestoreDb _firestoreDb;
        private readonly HttpClient _http;
        private readonly string _apiKey;
        public UserService(IConfiguration config)
        {
            _apiKey = config["Firebase:ApiKey"];
            _firestoreDb = FirestoreDb.Create(config["Firebase:ProjectId"]);
            _http = new HttpClient();

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

     
        public async Task UpdateUserAsync(UserModel user)
        {
            DocumentReference docRef = _firestoreDb.Collection("users").Document(user.Username);
            await docRef.SetAsync(user, SetOptions.MergeAll);
        }

        public async Task<List<UserModel>> GetAllUsersAsync()
        {
            var snapshot = await _firestoreDb.Collection("users").GetSnapshotAsync();
            return snapshot.Documents.Select(doc => doc.ConvertTo<UserModel>()).ToList();
        }

  

    }
}
