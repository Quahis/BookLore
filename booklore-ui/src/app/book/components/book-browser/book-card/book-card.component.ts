import {Component, inject, Input, OnInit} from '@angular/core';
import {Book} from '../../../model/book.model';
import {Button} from 'primeng/button';
import {MenuModule} from 'primeng/menu';
import {MenuItem} from 'primeng/api';
import {DialogService} from 'primeng/dynamicdialog';
import {ShelfAssignerComponent} from '../../shelf-assigner/shelf-assigner.component';
import {BookService} from '../../../service/book.service';
import {CheckboxModule} from 'primeng/checkbox';
import {FormsModule} from '@angular/forms';
import {MetadataDialogService} from '../../../../metadata/service/metadata-dialog.service';
import {MetadataFetchOptionsComponent} from '../../../../metadata/metadata-options-dialog/metadata-fetch-options/metadata-fetch-options.component';
import {MetadataRefreshType} from '../../../../metadata/model/request/metadata-refresh-type.enum';
import {MetadataRefreshRequest} from '../../../../metadata/model/request/metadata-refresh-request.model';
import {UrlHelperService} from '../../../../utilities/service/url-helper.service';
import {AsyncPipe, NgIf} from '@angular/common';
import {SecurePipe} from '../../../../secure-pipe';


@Component({
  selector: 'app-book-card',
  templateUrl: './book-card.component.html',
  styleUrls: ['./book-card.component.scss'],
  imports: [Button, MenuModule, CheckboxModule, FormsModule, NgIf, AsyncPipe, SecurePipe],
  standalone: true
})
export class BookCardComponent implements OnInit {
  @Input() book!: Book;
  @Input() isCheckboxEnabled: boolean = false;
  @Input() onBookSelect?: (bookId: number, selected: boolean) => void;
  @Input() isSelected: boolean = false;

  items: MenuItem[] | undefined;
  isHovered: boolean = false;

  private bookService = inject(BookService);
  private dialogService = inject(DialogService);
  private metadataDialogService = inject(MetadataDialogService);
  protected urlHelper = inject(UrlHelperService);

  ngOnInit(): void {
    this.initMenu();
  }

  readBook(book: Book): void {
    this.bookService.readBook(book.id);
  }

  toggleSelection(selected: boolean): void {
    if (this.isCheckboxEnabled) {
      this.isSelected = selected;
      if (this.onBookSelect) {
        this.onBookSelect(this.book.id, selected);
      }
    }
  }

  private initMenu() {
    this.items = [
      {
        label: 'Options',
        items: [
          {
            label: 'Assign Shelves',
            icon: 'pi pi-folder',
            command: () => this.openShelfDialog(this.book),
          },
          {
            label: 'View Details',
            icon: 'pi pi-info-circle',
            command: () => this.metadataDialogService.openBookMetadataCenterDialog(this.book.id, 'view'),
          },
          {
            label: 'Match Book',
            icon: 'pi pi-sparkles',
            command: () => this.metadataDialogService.openBookMetadataCenterDialog(this.book.id, 'match'),
          },
          {
            label: 'Quick Refresh',
            icon: 'pi pi-bolt',
            command: () => {
              const metadataRefreshRequest: MetadataRefreshRequest = {
                quick: true,
                refreshType: MetadataRefreshType.BOOKS,
                bookIds: [this.book.id]
              };
              this.bookService.autoRefreshMetadata(metadataRefreshRequest).subscribe();
            },
          },
          {
            label: 'Granular Refresh',
            icon: 'pi pi-database',
            command: () => {
              this.dialogService.open(MetadataFetchOptionsComponent, {
                header: 'Metadata Refresh Options',
                modal: true,
                closable: true,
                data: {
                  bookIds: [this.book!.id],
                  metadataRefreshType: MetadataRefreshType.BOOKS
                }
              })
            },
          },
          {
            label: 'Download',
            icon: 'pi pi-download',
            command: () => {
              this.bookService.downloadFile(this.book.id)
            },
          },
        ],
      },
    ];
  }

  private openShelfDialog(book: Book): void {
    this.dialogService.open(ShelfAssignerComponent, {
      header: `Update Book's Shelves`,
      modal: true,
      closable: true,
      contentStyle: {overflow: 'auto'},
      baseZIndex: 10,
      style: {
        position: 'absolute',
        top: '15%',
      },
      data: {
        book: this.book
      },
    });
  }

  openBookInfo(book: Book): void {
    this.metadataDialogService.openBookMetadataCenterDialog(book.id, 'view');
  }
}
