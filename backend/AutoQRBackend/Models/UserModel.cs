using Google.Cloud.Firestore;

namespace AutoQRBackend.Models;

[FirestoreData]
public class UserModel
{
	[FirestoreProperty] public string Email { get; set; }

	[FirestoreProperty] public string Username { get; set; }

	[FirestoreProperty] public string QrCode { get; set; }

	[FirestoreProperty] public string? ProfileImageUrl { get; set; }
	[FirestoreProperty] public string? DeviceToken { get; set; }
	[FirestoreProperty] public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

	[FirestoreDocumentId]
	public string Uid { get; set; }
}
