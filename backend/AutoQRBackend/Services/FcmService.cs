using Google.Apis.Auth.OAuth2;
using Google.Cloud.Firestore;
using Newtonsoft.Json;
using System.Net.Http.Headers;
using AutoQRBackend.Models;
using Microsoft.Extensions.Configuration;
using Google.Cloud.Firestore.V1;

namespace AutoQRBackend.Services
{
	public class FcmService
	{
		private readonly string _projectId;
		private readonly string _fcmUrl;
		private readonly FirestoreDb _firestoreDb;

		public FcmService(IConfiguration config)
		{
			_projectId = config["Firebase:ProjectId"]!;
			_fcmUrl = $"https://fcm.googleapis.com/v1/projects/{_projectId}/messages:send";
			var credentialsPath = "autoqr-4e823-firebase-adminsdk-fbsvc-bf73ebb448.json";
			Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", credentialsPath);
			_firestoreDb = FirestoreDb.Create(_projectId);
		}


		public async Task<bool> SendPushNotificationAsync(string deviceToken, string title, string body)
		{
			var credential = GoogleCredential
				 .FromFile("autoqr-4e823-firebase-adminsdk-fbsvc-bf73ebb448.json")
				 .CreateScoped("https://www.googleapis.com/auth/firebase.messaging");

			var accessToken = await credential.UnderlyingCredential.GetAccessTokenForRequestAsync();

			var httpClient = new HttpClient();
			httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", accessToken);

			var message = new
			{
				message = new
				{
					token = deviceToken,
					notification = new
					{
						title = title,
						body = body
					}
				}
			};

			var json = JsonConvert.SerializeObject(message);
			var content = new StringContent(json, System.Text.Encoding.UTF8, "application/json");

			var response = await httpClient.PostAsync(_fcmUrl, content);
			return response.IsSuccessStatusCode;
		}

		public async Task SaveMessageToFirestoreAsync(string fromUsername, string toUsername, string messageBody, string toUid, string messageType, string? replyToMessage = null)
		{
			var messageData = new Dictionary<string, object>
			{
				{ "fromUsername", fromUsername },
				{ "toUsername", toUsername },
				{ "message", messageBody },
				{ "messageType", messageType },
				{ "isRead", false },
				{ "timestamp", Timestamp.GetCurrentTimestamp() }
			};

			if (!string.IsNullOrWhiteSpace(replyToMessage))
			{
				messageData["replyTo"] = replyToMessage;
			}

			var docRef = _firestoreDb.Collection("messages").Document();
			messageData["id"] = docRef.Id;
			await docRef.SetAsync(messageData);


			var userInboxRef = _firestoreDb
					.Collection("users")
					.Document(toUid)
					.Collection("inbox")
					.Document(docRef.Id);


			await userInboxRef.SetAsync(messageData);
		}

		public async Task<bool> SendMessageAsync(string deviceToken, string fromUsername, string toUsername, string messageBody, string toUid, string messageType, string? replyToMessage = null)
		{
			var notificationSent = await SendPushNotificationAsync(deviceToken, "New Message", messageBody);
			if (notificationSent)
			{
				await SaveMessageToFirestoreAsync(fromUsername, toUsername, messageBody, toUid, messageType, replyToMessage);
				return true;
			}
			return false;
		}
	

	}
}
