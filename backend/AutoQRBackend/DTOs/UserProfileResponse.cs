namespace AutoQRBackend.DTOs;

public class UserProfileResponse
{
    public string Email { get; set; }
    public string Username { get; set; }
    public string QrCode { get; set; }
    public string ProfileImageUrl { get; set; }
    public string? DeviceToken { get; set; }
}
