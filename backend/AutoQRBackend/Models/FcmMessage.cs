namespace AutoQRBackend.Models
{
    public class FcmMessage
    {
        public required string To { get; set; }  
        public required Notification Notification { get; set; }
        public Dictionary<string, string> Data { get; set; } = new();
    }
}
