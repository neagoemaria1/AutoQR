namespace AutoQRBackend.DTOs
{
    public class SendMessageRequest
    {
        public required string FromUsername { get; set; }  
        public required string ToQrCode { get; set; }     
        public required string Body { get; set; }
		  public string? MessageType { get; set; }
		  public string? ReplyToMessage { get; set; }
	}
}
